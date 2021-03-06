package tourGuide.service;

import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.exception.NotFoundException;
import tourGuide.model.beans.AttractionBean;
import tourGuide.model.beans.LocationBean;
import tourGuide.model.beans.ProviderBean;
import tourGuide.model.beans.VisitedLocationBean;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.DTO.ClosestAttractionDTO;
import tourGuide.model.DTO.UserCurrentLocationDTO;
import tourGuide.model.DTO.UserPreferencesDTO;
import tourGuide.proxies.GpsMicroserviceProxy;
import tourGuide.proxies.TripPricerMicroserviceProxy;
import tourGuide.tracker.Tracker;
import tourGuide.model.user.User;
import tourGuide.model.user.UserPreferences;
import tourGuide.model.user.UserReward;

import javax.money.Monetary;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TourGuideService {
	private static final Logger LOGGER = LoggerFactory.getLogger(TourGuideService.class);
	@Autowired
	private GpsMicroserviceProxy gpsMicroserviceProxy;
	@Autowired
	private TripPricerMicroserviceProxy tripPricerMicroserviceProxy;

	private final RewardsService rewardsService;

	public final Tracker tracker;
	boolean testMode = true;

	private final ExecutorService executorGps = Executors.newFixedThreadPool(150);


	public TourGuideService(GpsMicroserviceProxy gpsMicroserviceProxy, RewardsService rewardsService,
                            TripPricerMicroserviceProxy tripPricerMicroserviceProxy) {
		this.gpsMicroserviceProxy = gpsMicroserviceProxy;
		this.rewardsService = rewardsService;
		this.tripPricerMicroserviceProxy = tripPricerMicroserviceProxy;

		if(testMode) {
			LOGGER.info("TestMode enabled");
			LOGGER.debug("Initializing users");
			initializeInternalUsers();
			LOGGER.debug("Finished initializing users");
		}
		tracker = new Tracker(this, gpsMicroserviceProxy);
		addShutDownHook();
	}

	/**
	 * Gives the user list of rewards
	 * @param userName of the user
	 * @return
	 */
	public List<UserReward> getUserRewards(String userName) {
		return getUser(userName).getUserRewards();
	}

	/**
	 * Gives the last visited location
	 * @param user
	 * @return
	 */
	public VisitedLocationBean getLastVisitedLocation(User user) {
		int locationNumber = user.getVisitedLocations().size()-1;
		if (locationNumber < 0) {
			return null;
		}
		return user.getVisitedLocations().get(locationNumber);
	}

	/**
	 * Gives the user location
	 * @param userName of the user
	 * @return the visited location
	 * @throws InterruptedException
	 */
	public VisitedLocationBean getUserLocation(String userName) throws InterruptedException {
		User user = getUser(userName);

		if (user.getVisitedLocations().size() == 0) {
			CountDownLatch trackLatch = new CountDownLatch( 1 );
			getUserNewLocation(user, trackLatch);
			trackLatch.await();
		}

		return getLastVisitedLocation(user);
	}

	/**
	 * Tracks a user to get a new visited Location
	 * @param user to be tracked
	 * @param trackLatch countdown latch
	 */
	public void getUserNewLocation(User user, CountDownLatch trackLatch) {
		CompletableFuture.supplyAsync(() -> gpsMicroserviceProxy.getUserLocation(user.getUserId()), executorGps)
				.thenAccept(loc -> updateUserVisitedLocationData(loc, user, trackLatch));

	}

	/**
	 * Updates a user (VisitedLocation + Rewards) when a new visited location is tracked
	 * @param loc new visited location
	 * @param user to be updated
	 * @param trackLatch countdown latch
	 */
	public void updateUserVisitedLocationData (VisitedLocationBean loc, User user, CountDownLatch trackLatch) {

		user.addToVisitedLocations(loc);
		rewardsService.calculateRewards(user);
		// one user updated
		trackLatch.countDown();

	}

	/**
	 * Gives a user from his userName
	 * @param userName
	 * @return
	 */
	public User getUser(String userName) {
		User user = internalUserMap.get(userName);
		if (user == null) {
			throw new NotFoundException("user " + userName + " not found");
		}

		return user;
	}

	/**
	 * Gives all the users
	 * @return
	 */
	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	/**
	 * Add a user
	 * @param user
	 */
	public void addUser(User user) {
		if(!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	/**
	 * Queries TripPricer to get 5 providers for a given attraction
	 * @param userName
	 * @param attractionUUID
	 * @return the list of providers
	 */
	public List<ProviderBean> getTripDeals(String userName, UUID attractionUUID) {
		User user = getUser(userName);

		int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
		List<ProviderBean> providers = tripPricerMicroserviceProxy.getProviderList(tripPricerApiKey, attractionUUID, user.getUserPreferences().getNumberOfAdults(),
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	/**
	 * Retrieves the current location of all connected users
	 * @return the list
	 */
	public List<UserCurrentLocationDTO> getAllCurrentLocations() {
		List<User> users = getAllUsers();
		List<UserCurrentLocationDTO> userCurrentLocationDTOs = users.parallelStream()
				.map(user -> {
					try {
						return new UserCurrentLocationDTO(user.getUserId().toString(), getUserLocation(user.getUserName()).location);
					} catch (InterruptedException e) {
						LOGGER.error("getLocation - Error during retrieving user location");
					}
					return null;
				})
				.collect(Collectors.toList());
		return userCurrentLocationDTOs;
	}

	/**
	 * Assures to shut down the Gps thread
	 */
	public void stopGps() {
		executorGps.shutdownNow();
	}

	/**
	 * To stop the tracker
	 */
	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() { 
		      public void run() {
		        tracker.stopTracking();
				stopGps();
		      } 
		    }); 
	}

	/**
	 * Updates the user preferences
	 * @param userName
	 * @return the updated user
	 */
	public User updateUserPreferences(String userName, UserPreferencesDTO userPreferencesDTO) {
		User user = getUser(userName);

		UserPreferences userPreferences = user.getUserPreferences();
		userPreferences.setAttractionProximity(userPreferencesDTO.getAttractionProximity());
		userPreferences.setCurrency(Monetary.getCurrency(userPreferencesDTO.getCurrency()));
		userPreferences.setHighPricePoint(Money.of(userPreferencesDTO.getHighPricePoint(), userPreferencesDTO.getCurrency()));
		userPreferences.setLowerPricePoint(Money.of(userPreferencesDTO.getLowerPricePoint(), userPreferencesDTO.getCurrency()));
		userPreferences.setNumberOfAdults(userPreferencesDTO.getNumberOfAdults());
		userPreferences.setNumberOfChildren(userPreferencesDTO.getNumberOfChildren());
		userPreferences.setTicketQuantity(userPreferencesDTO.getTicketQuantity());
		userPreferences.setTripDuration(userPreferencesDTO.getTripDuration());
		user.setUserPreferences(userPreferences);

		return user;
	}

	/**
	 * Gives the user preferences
	 * @param userName
	 * @return user preferences
	 */
	public UserPreferencesDTO getUserPreferences(String userName) {
		User user = getUser(userName);
		UserPreferencesDTO userPreferencesDTO = new UserPreferencesDTO();


		UserPreferences userPreferences = user.getUserPreferences();
		userPreferencesDTO.setAttractionProximity(userPreferences.getAttractionProximity());
		userPreferencesDTO.setCurrency(userPreferences.getCurrency().getCurrencyCode());
		userPreferencesDTO.setHighPricePoint(userPreferences.getHighPricePoint().getNumber().intValue());
		userPreferencesDTO.setLowerPricePoint(userPreferences.getLowerPricePoint().getNumber().intValue());
		userPreferencesDTO.setNumberOfAdults(userPreferences.getNumberOfAdults());
		userPreferencesDTO.setNumberOfChildren(userPreferences.getNumberOfChildren());
		userPreferencesDTO.setTicketQuantity(userPreferences.getTicketQuantity());
		userPreferencesDTO.setTripDuration(userPreferences.getTripDuration());


		return userPreferencesDTO;
	}

	/**
	 * Gives the 5 closest attractions of the given visited location
	 * @param visitedLocation
	 * @return The list of attraction
	 */
	public List<ClosestAttractionDTO> getNearByAttractions(VisitedLocationBean visitedLocation) {
		List<AttractionBean> attractions = gpsMicroserviceProxy.getAttractionsList();

		List<ClosestAttractionDTO> closestAttractionDTOs = attractions.parallelStream()
				//Create a ClosestAttractionDTO form an Attraction, calculating the distance Attraction/User
				.map(attraction -> new ClosestAttractionDTO(attraction.attractionName,
						new LocationBean(attraction.latitude, attraction.longitude),
						rewardsService.getDistance(new LocationBean(attraction.latitude, attraction.longitude), visitedLocation.location)
						,attraction.attractionId))
				//Sort the ClosestAttractionDTOs from the nearest to the farthest
				.sorted(Comparator.comparing(ClosestAttractionDTO::getDistance))
				//take the 5 nearest
				.limit(5)
				.collect(Collectors.toList());

		closestAttractionDTOs = closestAttractionDTOs.parallelStream()
				.peek(attraction -> {
					attraction.setVisitedLocation(visitedLocation.location);
					attraction.setRewardPoints(rewardsService.getRewardCentralPoints(attraction.getAttractionId(), visitedLocation.userId));
				})
				.collect(Collectors.toList());

		return closestAttractionDTOs;
	}

	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();
	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);
			
			internalUserMap.put(userName, user);
		});
		LOGGER.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}
	
	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i-> {
			user.addToVisitedLocations(new VisitedLocationBean(user.getUserId(), new LocationBean(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}
	
	private double generateRandomLongitude() {
		double leftLimit = -180;
	    double rightLimit = 180;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
	    double rightLimit = 85.05112878;
	    return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}
	
	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
	    return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}
}

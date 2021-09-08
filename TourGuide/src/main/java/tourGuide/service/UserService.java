package tourGuide.service;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.javamoney.moneta.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.DTO.UserCurrentLocationDTO;
import tourGuide.model.DTO.UserPreferencesDTO;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

import javax.money.Monetary;
import javax.money.UnknownCurrencyException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class UserService {
	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
	private final GpsService gpsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;
	
	public UserService(GpsService gpsService) {
		this.gpsService = gpsService;

		if(testMode) {
			LOGGER.info("TestMode enabled");
			LOGGER.debug("Initializing users");
			initializeInternalUsers();
			LOGGER.debug("Finished initializing users");
		}
		tracker = new Tracker(this, gpsService);
		addShutDownHook();
	}
	
	public List<UserReward> getUserRewards(String userName) {
		return getUser(userName).getUserRewards();
	}

	public VisitedLocation getLastVisitedLocation(User user) {
		int locationNumber = user.getVisitedLocations().size()-1;
		if (locationNumber < 0) {
			return null;
		}
		return user.getVisitedLocations().get(locationNumber);
	}

	public VisitedLocation getUserLocation(String userName) throws InterruptedException {
		User user = getUser(userName);

		if (user == null) {
			return null;
		}

		if (user.getVisitedLocations().size() == 0) {
			CountDownLatch trackLatch = new CountDownLatch( 1 );
			gpsService.trackUserLocation(user, trackLatch);
			trackLatch.await();
		}

		return getLastVisitedLocation(user);
	}
	
	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}
	
	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}
	
	public void addUser(User user) {
		if(!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}
	
	public List<Provider> getTripDeals(String userName, String attractionName) {
		User user = getUser(userName);
		//tripPricer.getPrice use attractionId and not UserId.
		//We don't have the tools to retrieve attractionId from attractionName so I have let UserId ...

		int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(), user.getUserPreferences().getNumberOfAdults(), 
				user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}
	
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

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() { 
		      public void run() {
		        tracker.stopTracking();
		      } 
		    }); 
	}

	public User updateUserPreferences(String userName, UserPreferencesDTO userPreferencesDTO) throws UnknownCurrencyException {
		User user = getUser(userName);
		if (user != null) {
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
		}
		return user;
	}

	public UserPreferencesDTO getUserPreferences(String userName) {
		User user = getUser(userName);
		UserPreferencesDTO userPreferencesDTO = new UserPreferencesDTO();

		if (user == null) {
			//attractionProximity to be tested to check the existence of the user
			userPreferencesDTO.setAttractionProximity(-1);
		} else {
			UserPreferences userPreferences = user.getUserPreferences();
			userPreferencesDTO.setAttractionProximity(userPreferences.getAttractionProximity());
			userPreferencesDTO.setCurrency(userPreferences.getCurrency().getCurrencyCode());
			userPreferencesDTO.setHighPricePoint(userPreferences.getHighPricePoint().getNumber().intValue());
			userPreferencesDTO.setLowerPricePoint(userPreferences.getLowerPricePoint().getNumber().intValue());
			userPreferencesDTO.setNumberOfAdults(userPreferences.getNumberOfAdults());
			userPreferencesDTO.setNumberOfChildren(userPreferences.getNumberOfChildren());
			userPreferencesDTO.setTicketQuantity(userPreferences.getTicketQuantity());
			userPreferencesDTO.setTripDuration(userPreferences.getTripDuration());

		}
		return userPreferencesDTO;
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
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
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

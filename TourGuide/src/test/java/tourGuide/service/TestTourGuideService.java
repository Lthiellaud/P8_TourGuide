package tourGuide.service;

import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.exception.NotFoundException;
import tourGuide.model.DTO.ClosestAttractionDTO;
import tourGuide.model.beans.ProviderBean;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.DTO.UserCurrentLocationDTO;
import tourGuide.model.DTO.UserPreferencesDTO;
import tourGuide.proxies.GpsMicroserviceProxy;
import tourGuide.proxies.RewardsMicroserviceProxy;
import tourGuide.proxies.TripPricerMicroserviceProxy;
import tourGuide.model.user.User;
import tourGuide.model.user.UserPreferences;

import javax.money.Monetary;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestTourGuideService {

	@Autowired
	GpsMicroserviceProxy gpsMicroserviceProxy;

	@Autowired
	RewardsMicroserviceProxy rewardsMicroserviceProxy;

	@Autowired
	TripPricerMicroserviceProxy tripPricerMicroserviceProxy;

	@Test
	public void getUserLocation() throws InterruptedException {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		CountDownLatch trackLatch = new CountDownLatch( 1 );
		tourGuideService.getUserNewLocation(user, trackLatch);
		trackLatch.await();
		tourGuideService.tracker.stopTracking();
		tourGuideService.stopGps();

		assertEquals(user.getVisitedLocations().get(0).userId, user.getUserId());
	}
	
	@Test
	public void addUser() {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		User retrievedUser = tourGuideService.getUser(user.getUserName());
		User retrievedUser2 = tourGuideService.getUser(user2.getUserName());

		tourGuideService.tracker.stopTracking();
		tourGuideService.stopGps();

		assertEquals(user, retrievedUser);
		assertEquals(user2, retrievedUser2);
	}
	
	@Test
	public void getAllUsers() {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		tourGuideService.addUser(user);
		tourGuideService.addUser(user2);
		
		List<User> allUsers = tourGuideService.getAllUsers();

		tourGuideService.tracker.stopTracking();
		tourGuideService.stopGps();

		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}
	
	@Test
	public void getTripDeals() {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		List<ProviderBean> providers = tourGuideService.getTripDeals("internalUser0", UUID.randomUUID());
		
		tourGuideService.tracker.stopTracking();
		tourGuideService.stopGps();

		assertEquals(5, providers.size());
	}

	@Test
	public void getAllCurrentLocations() {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(4);
		TourGuideService tourGuideService = new TourGuideService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		List<UserCurrentLocationDTO> userCurrentLocationDTOs = tourGuideService.getAllCurrentLocations();

		tourGuideService.tracker.stopTracking();
		tourGuideService.stopGps();

		assertEquals(4,userCurrentLocationDTOs.size());
	}

	@Test
	public void getUserPreferencesNull() {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		try {
			tourGuideService.getUserPreferences("internalUser2");
		} catch (NotFoundException e) {
			assertEquals("user internalUser2 not found", e.getMessage());
		}
		tourGuideService.tracker.stopTracking();
		tourGuideService.stopGps();

	}

	@Test
	public void getUserPreferences() {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		UserPreferencesDTO userPreferencesDTO = tourGuideService.getUserPreferences("internalUser0");

		tourGuideService.tracker.stopTracking();
		tourGuideService.stopGps();

		assertEquals(Integer.MAX_VALUE, userPreferencesDTO.getAttractionProximity());
		assertEquals(1,userPreferencesDTO.getTripDuration());
		assertEquals(0,userPreferencesDTO.getNumberOfChildren());
		assertEquals(1,userPreferencesDTO.getNumberOfAdults());
		assertEquals("USD",userPreferencesDTO.getCurrency());
		assertEquals(Integer.MAX_VALUE,userPreferencesDTO.getHighPricePoint());
		assertEquals(0 ,userPreferencesDTO.getLowerPricePoint());
		assertEquals(1,userPreferencesDTO.getTicketQuantity());
	}

	@Test
	public void updateUserPreferences() {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(1);
		TourGuideService tourGuideService = new TourGuideService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		UserPreferencesDTO userPreferences = new UserPreferencesDTO();
		userPreferences.setTripDuration(7);
		userPreferences.setNumberOfChildren(3);
		userPreferences.setNumberOfAdults(2);
		userPreferences.setAttractionProximity(10);
		userPreferences.setCurrency("EUR");
		userPreferences.setHighPricePoint(500);
		userPreferences.setLowerPricePoint(100);
		userPreferences.setTicketQuantity((5));

		User user = tourGuideService.updateUserPreferences("internalUser0", userPreferences);

		tourGuideService.tracker.stopTracking();
		tourGuideService.stopGps();

		UserPreferences newUserPreferences = user.getUserPreferences();
		assertEquals(7,newUserPreferences.getTripDuration());
		assertEquals(3,newUserPreferences.getNumberOfChildren());
		assertEquals(2,newUserPreferences.getNumberOfAdults());
		assertEquals(10,newUserPreferences.getAttractionProximity());
		assertEquals(Monetary.getCurrency("EUR"),newUserPreferences.getCurrency());
		assertEquals(Money.of(500, Monetary.getCurrency("EUR")),newUserPreferences.getHighPricePoint());
		assertEquals(Money.of(100, Monetary.getCurrency("EUR")) ,newUserPreferences.getLowerPricePoint());
		assertEquals(5,newUserPreferences.getTicketQuantity());
	}

	@Test
	public void getNearbyAttractions() throws InterruptedException {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(0);

		TourGuideService tourGuideService = new TourGuideService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		tourGuideService.addUser(user);
		List<ClosestAttractionDTO> closestAttractionDTOs = tourGuideService.getNearByAttractions(tourGuideService.getUserLocation("jon"));

		tourGuideService.tracker.stopTracking();
		tourGuideService.stopGps();

		assertEquals(5, closestAttractionDTOs.size());
	}

	@Test
	public void trackUser() throws InterruptedException {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(0);
		TourGuideService tourGuideService = new TourGuideService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		CountDownLatch trackLatch = new CountDownLatch( 1 );
		tourGuideService.getUserNewLocation(user, trackLatch);
		trackLatch.await();

		tourGuideService.tracker.stopTracking();
		tourGuideService.stopGps();

		assertEquals(user.getUserId(), tourGuideService.getLastVisitedLocation(user).userId);
		assertEquals(user.getVisitedLocations().size(), 1);
	}

}

package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import org.javamoney.moneta.Money;
import org.junit.Test;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.DTO.UserCurrentLocationDTO;
import tourGuide.model.DTO.UserPreferencesDTO;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.Provider;

import javax.money.Monetary;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestUserService {

	@Test
	public void getUserLocation() throws ExecutionException, InterruptedException {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		GpsService gpsService = new GpsService(gpsUtil, rewardsService);
		UserService userService = new UserService(gpsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		VisitedLocation visitedLocation = gpsService.trackUserLocation(user);
		userService.tracker.stopTracking();
		assertEquals(visitedLocation.userId, user.getUserId());
	}
	
	@Test
	public void addUser() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		GpsService gpsService = new GpsService(gpsUtil, rewardsService);
		UserService userService = new UserService(gpsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		userService.addUser(user);
		userService.addUser(user2);
		
		User retrievedUser = userService.getUser(user.getUserName());
		User retrievedUser2 = userService.getUser(user2.getUserName());

		userService.tracker.stopTracking();
		
		assertEquals(user, retrievedUser);
		assertEquals(user2, retrievedUser2);
	}
	
	@Test
	public void getAllUsers() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(0);
		GpsService gpsService = new GpsService(gpsUtil, rewardsService);
		UserService userService = new UserService(gpsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		User user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

		userService.addUser(user);
		userService.addUser(user2);
		
		List<User> allUsers = userService.getAllUsers();

		userService.tracker.stopTracking();
		
		assertTrue(allUsers.contains(user));
		assertTrue(allUsers.contains(user2));
	}
	
	@Test
	public void getTripDeals() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(1);
		GpsService gpsService = new GpsService(gpsUtil, rewardsService);
		UserService userService = new UserService(gpsService);

		List<Provider> providers = userService.getTripDeals("internalUser0", "Disneyland");
		
		userService.tracker.stopTracking();
		
		assertEquals(5, providers.size());
	}

	@Test
	public void getAllCurrentLocations() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(4);
		GpsService gpsService = new GpsService(gpsUtil, rewardsService);
		UserService userService = new UserService(gpsService);

		List<UserCurrentLocationDTO> userCurrentLocationDTOs = userService.getAllCurrentLocations();

		assertEquals(4,userCurrentLocationDTOs.size());
	}

	@Test
	public void updateUserPreferences() {
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		InternalTestHelper.setInternalUserNumber(1);
		GpsService gpsService = new GpsService(gpsUtil, rewardsService);
		UserService userService = new UserService(gpsService);

		UserPreferencesDTO userPreferences = new UserPreferencesDTO();
		userPreferences.setTripDuration(7);
		userPreferences.setNumberOfChildren(3);
		userPreferences.setNumberOfAdults(2);
		userPreferences.setAttractionProximity(10);
		userPreferences.setCurrency("EUR");
		userPreferences.setHighPricePoint(500);
		userPreferences.setLowerPricePoint(100);
		userPreferences.setTicketQuantity((5));

		User user = userService.updateUserPreferences("internalUser0", userPreferences);

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
}

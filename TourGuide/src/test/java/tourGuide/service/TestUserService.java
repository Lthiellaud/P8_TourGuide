package tourGuide.service;

import org.javamoney.moneta.Money;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.beans.ProviderBean;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.DTO.UserCurrentLocationDTO;
import tourGuide.model.DTO.UserPreferencesDTO;
import tourGuide.proxies.GpsMicroserviceProxy;
import tourGuide.proxies.RewardsMicroserviceProxy;
import tourGuide.proxies.TripPricerMicroserviceProxy;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;

import javax.money.Monetary;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestUserService {

	@Autowired
	GpsMicroserviceProxy gpsMicroserviceProxy;

	@Autowired
	RewardsMicroserviceProxy rewardsMicroserviceProxy;

	@Autowired
	TripPricerMicroserviceProxy tripPricerMicroserviceProxy;

	@Test
	public void getUserLocation() throws InterruptedException {
		Locale englishLocale = new Locale("en", "EN");
		Locale.setDefault(englishLocale);
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(0);
		UserService userService = new UserService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		CountDownLatch trackLatch = new CountDownLatch( 1 );
		userService.getNewUserLocation(user, trackLatch);
		trackLatch.await();
		userService.tracker.stopTracking();
		assertEquals(user.getVisitedLocations().get(0).userId, user.getUserId());
	}
	
	@Test
	public void addUser() {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(0);
		UserService userService = new UserService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

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
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(0);
		UserService userService = new UserService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

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
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(1);
		UserService userService = new UserService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		List<ProviderBean> providers = userService.getTripDeals("internalUser0", "Disneyland");
		
		userService.tracker.stopTracking();
		
		assertEquals(5, providers.size());
	}

	@Test
	public void getAllCurrentLocations() {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(4);
		UserService userService = new UserService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		List<UserCurrentLocationDTO> userCurrentLocationDTOs = userService.getAllCurrentLocations();

		userService.tracker.stopTracking();

		assertEquals(4,userCurrentLocationDTOs.size());
	}

	@Test
	public void getUserPreferencesNull() {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(1);
		UserService userService = new UserService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		UserPreferencesDTO userPreferencesDTO = userService.getUserPreferences("internalUser2");

		userService.tracker.stopTracking();

		assertEquals(-1, userPreferencesDTO.getAttractionProximity());

	}

	@Test
	public void getUserPreferences() {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		InternalTestHelper.setInternalUserNumber(1);
		UserService userService = new UserService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		UserPreferencesDTO userPreferencesDTO = userService.getUserPreferences("internalUser0");

		userService.tracker.stopTracking();

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
		UserService userService = new UserService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

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

		userService.tracker.stopTracking();

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

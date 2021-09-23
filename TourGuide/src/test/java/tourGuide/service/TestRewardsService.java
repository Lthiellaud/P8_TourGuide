package tourGuide.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tourGuide.beans.AttractionBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.helper.InternalTestHelper;
import tourGuide.proxies.GpsMicroserviceProxy;
import tourGuide.proxies.RewardsMicroserviceProxy;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest
public class TestRewardsService {

	@Autowired
	GpsMicroserviceProxy gpsMicroserviceProxy;

	@Autowired
	RewardsMicroserviceProxy rewardsMicroserviceProxy;

	@Test
	public void userGetRewards() throws InterruptedException {
		Locale englishLocale = new Locale("en", "EN");
		Locale.setDefault(englishLocale);
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);

		InternalTestHelper.setInternalUserNumber(0);
		UserService userService = new UserService(gpsMicroserviceProxy, rewardsService);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		AttractionBean attraction = gpsMicroserviceProxy.getAttractionsList().get(0);
		user.addToVisitedLocations(new VisitedLocationBean(user.getUserId(), attraction, new Date()));

		CountDownLatch trackLatch = new CountDownLatch( 1 );
		userService.getNewUserLocation(user, trackLatch);
		trackLatch.await();

		List<UserReward> userRewards = user.getUserRewards();
		userService.tracker.stopTracking();
		assertEquals(1, userRewards.size());
	}
	
	@Test
	public void isWithinAttractionProximity() {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		AttractionBean attraction = gpsMicroserviceProxy.getAttractionsList().get(0);
		assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
	}
	
	@Test
	public void nearAllAttractions() {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		rewardsService.setProximityBuffer(Integer.MAX_VALUE);

		InternalTestHelper.setInternalUserNumber(1);
		UserService userService = new UserService(gpsMicroserviceProxy, rewardsService);

		rewardsService.calculateRewards(userService.getAllUsers().get(0));

		List<UserReward> userRewards = userService.getUserRewards(userService.getAllUsers().get(0).getUserName());
		userService.tracker.stopTracking();

		assertEquals(gpsMicroserviceProxy.getAttractionsList().size(), userRewards.size());
	}
	
}

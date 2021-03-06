package tourGuide.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.model.beans.AttractionBean;
import tourGuide.model.beans.VisitedLocationBean;
import tourGuide.helper.InternalTestHelper;
import tourGuide.proxies.GpsMicroserviceProxy;
import tourGuide.proxies.RewardsMicroserviceProxy;
import tourGuide.proxies.TripPricerMicroserviceProxy;
import tourGuide.model.user.User;
import tourGuide.model.user.UserReward;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestRewardsService {

	@Autowired
	GpsMicroserviceProxy gpsMicroserviceProxy;

	@Autowired
	RewardsMicroserviceProxy rewardsMicroserviceProxy;

	@Autowired
	TripPricerMicroserviceProxy tripPricerMicroserviceProxy;

	@Test
	public void userGetRewards() throws InterruptedException {
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);

		InternalTestHelper.setInternalUserNumber(0);

		TourGuideService tourGuideService = new TourGuideService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
		AttractionBean attraction = gpsMicroserviceProxy.getAttractionsList().get(0);
		user.addToVisitedLocations(new VisitedLocationBean(user.getUserId(), attraction, new Date()));

		CountDownLatch trackLatch = new CountDownLatch( 1 );
		tourGuideService.getUserNewLocation(user, trackLatch);
		trackLatch.await();

		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.tracker.stopTracking();
		tourGuideService.stopGps();

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
		TourGuideService tourGuideService = new TourGuideService(gpsMicroserviceProxy, rewardsService, tripPricerMicroserviceProxy);

		rewardsService.calculateRewards(tourGuideService.getAllUsers().get(0));

		List<UserReward> userRewards = tourGuideService.getUserRewards(tourGuideService.getAllUsers().get(0).getUserName());
		tourGuideService.tracker.stopTracking();
		tourGuideService.stopGps();

		assertEquals(gpsMicroserviceProxy.getAttractionsList().size(), userRewards.size());
	}
	
}

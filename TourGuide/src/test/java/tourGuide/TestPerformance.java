package tourGuide;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tourGuide.beans.AttractionBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.helper.InternalTestHelper;
import tourGuide.proxies.GpsMicroserviceProxy;
import tourGuide.proxies.RewardsMicroserviceProxy;
import tourGuide.service.RewardsService;
import tourGuide.service.UserService;
import tourGuide.user.User;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class TestPerformance {
	
	/*
	 * A note on performance improvements:
	 *     
	 *     The number of users generated for the high volume tests can be easily adjusted via this method:
	 *     
	 *     		InternalTestHelper.setInternalUserNumber(100000);
	 *     
	 *     
	 *     These tests can be modified to suit new solutions, just as long as the performance metrics
	 *     at the end of the tests remains consistent. 
	 * 
	 *     These are performance metrics that we are trying to hit:
	 *     
	 *     highVolumeTrackLocation: 100,000 users within 15 minutes:
	 *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
	 *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	 */
	@Autowired
	GpsMicroserviceProxy gpsMicroserviceProxy;

	@Autowired
	RewardsMicroserviceProxy rewardsMicroserviceProxy;

	@Ignore
	@Test
	public void highVolumeTrackLocation() {
		Locale englishLocale = new Locale("en", "EN");
		Locale.setDefault(englishLocale);
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(100000);
		UserService userService = new UserService(gpsMicroserviceProxy, rewardsService);

		List<User> allUsers = userService.getAllUsers();

		CountDownLatch trackLatch = new CountDownLatch( allUsers.size() );
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		try {
			allUsers.forEach(user -> userService.getNewUserLocation(user, trackLatch));
			trackLatch.await();
		} catch (InterruptedException e) {
			System.out.println("getLocation - Error during retrieving user location");
		}

		stopWatch.stop();
		userService.tracker.stopTracking();

		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	@Ignore
	@Test
	public void highVolumeGetRewards() {
		Locale englishLocale = new Locale("en", "EN");
		Locale.setDefault(englishLocale);
		RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalTestHelper.setInternalUserNumber(100000);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		UserService userService = new UserService(gpsMicroserviceProxy, rewardsService);

		AttractionBean attraction = gpsMicroserviceProxy.getAttractionsList().get(0);
		List<User> allUsers = userService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocationBean(u.getUserId(), attraction, new Date())));

		CountDownLatch trackLatch = new CountDownLatch( allUsers.size() );

		try {
			allUsers.forEach(user -> userService.getNewUserLocation(user, trackLatch));
			trackLatch.await();
		} catch (InterruptedException e) {
			System.out.println("getLocation - Error during retrieving user location & rewards");
		}

//		allUsers.forEach(u -> rewardsService.calculateRewards(u));

	    for(User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}

		stopWatch.stop();
		userService.tracker.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
	
}

package tourGuide;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Ignore;
import org.junit.Test;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.GpsService;
import tourGuide.service.RewardsService;
import tourGuide.service.UserService;
import tourGuide.user.User;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
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

	//@Ignore
	@Test
	public void highVolumeTrackLocation() {
		Locale englishLocale = new Locale("en", "EN");
		Locale.setDefault(englishLocale);
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(100000);
		GpsService gpsService = new GpsService(gpsUtil, rewardsService);
		UserService userService = new UserService(gpsService);

		List<User> allUsers = userService.getAllUsers();
		System.out.println("BEFORE user 9 visitedLocation number " + allUsers.get(9).getVisitedLocations().size());
		allUsers.get(9).getVisitedLocations()
				.forEach(loc -> System.out.println("List BEFORE " + allUsers.get(9).getUserName()
						+ " - loc : " + loc.location.longitude + ", " + loc.location.latitude));

		CountDownLatch trackLatch = new CountDownLatch( allUsers.size() );
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		try {
			allUsers.forEach(user -> gpsService.trackUserLocation(user, trackLatch));
			trackLatch.await();
		} catch (InterruptedException e) {
			System.out.println("getLocation - Error during retrieving user location");
		}

		stopWatch.stop();
		userService.tracker.stopTracking();

		System.out.println("AFTER user 9 visitedLocation number " + allUsers.get(9).getVisitedLocations().size());
		allUsers.get(9).getVisitedLocations()
				.forEach(loc -> System.out.println("List AFTER " +  allUsers.get(9).getUserName()
						+ " - loc : " + loc.location.longitude + ", " + loc.location.latitude));
		System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	//@Ignore
	@Test
	public void highVolumeGetRewards() {
		Locale englishLocale = new Locale("en", "EN");
		Locale.setDefault(englishLocale);
		GpsUtil gpsUtil = new GpsUtil();
		RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());

		// Users should be incremented up to 100,000, and test finishes within 20 minutes
		InternalTestHelper.setInternalUserNumber(100000);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		GpsService gpsService = new GpsService(gpsUtil, rewardsService);
		UserService userService = new UserService(gpsService);

		Attraction attraction = gpsUtil.getAttractions().get(0);
		List<User> allUsers = userService.getAllUsers();
		allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

		allUsers.forEach(u -> rewardsService.calculateRewards(u));

	    for(User user : allUsers) {
			assertTrue(user.getUserRewards().size() > 0);
		}
		stopWatch.stop();
		userService.tracker.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
	
}

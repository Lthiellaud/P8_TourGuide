package tourGuide.service;

import gpsUtil.GpsUtil;
import org.junit.Test;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.DTO.ClosestAttractionDTO;
import tourGuide.user.User;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class TestGpsService {

    @Test
    public void getNearbyAttractions() throws InterruptedException {
        Locale englishLocale = new Locale("en", "EN");
        Locale.setDefault(englishLocale);
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        GpsService gpsService = new GpsService(gpsUtil, rewardsService);
        UserService userService = new UserService(gpsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        userService.addUser(user);
        List<ClosestAttractionDTO> closestAttractionDTOs = gpsService.getNearByAttractions(userService.getUserLocation("jon"));

        userService.tracker.stopTracking();

        assertEquals(5, closestAttractionDTOs.size());
    }

    @Test
    public void trackUser() throws ExecutionException, InterruptedException {
        Locale englishLocale = new Locale("en", "EN");
        Locale.setDefault(englishLocale);
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        GpsService gpsService = new GpsService(gpsUtil, rewardsService);
        UserService userService = new UserService(gpsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        CountDownLatch trackLatch = new CountDownLatch( 1 );
        gpsService.trackUserLocation(user, trackLatch);
        trackLatch.await();

        userService.tracker.stopTracking();

        assertEquals(user.getUserId(), userService.getLastVisitedLocation(user).userId);
    }



}

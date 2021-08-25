package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.VisitedLocation;
import org.junit.Test;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.DTO.ClosestAttractionDTO;
import tourGuide.user.User;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class TestGpsService {

    @Test
    public void getNearbyAttractions() throws ExecutionException, InterruptedException {
        Locale englishLocale = new Locale("en", "EN");
        Locale.setDefault(englishLocale);
        GpsUtil gpsUtil = new GpsUtil();
        RewardsService rewardsService = new RewardsService(gpsUtil, new RewardCentral());
        InternalTestHelper.setInternalUserNumber(0);
        GpsService gpsService = new GpsService(gpsUtil, rewardsService);
        UserService userService = new UserService(gpsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = gpsService.trackUserLocation(user);

        List<ClosestAttractionDTO> closestAttractionDTOs = gpsService.getNearByAttractions(visitedLocation);

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
        VisitedLocation visitedLocation = gpsService.trackUserLocation(user);

        userService.tracker.stopTracking();

        assertEquals(user.getUserId(), visitedLocation.userId);
    }



}

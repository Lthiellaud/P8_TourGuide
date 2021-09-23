package tourGuide.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.DTO.ClosestAttractionDTO;
import tourGuide.proxies.GpsMicroserviceProxy;
import tourGuide.proxies.RewardsMicroserviceProxy;
import tourGuide.user.User;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestGpsService {

    @Autowired
    GpsMicroserviceProxy gpsMicroserviceProxy;

    @Autowired
    RewardsMicroserviceProxy rewardsMicroserviceProxy;

    @Test
    public void getNearbyAttractions() throws InterruptedException {
        Locale englishLocale = new Locale("en", "EN");
        Locale.setDefault(englishLocale);
        RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
        InternalTestHelper.setInternalUserNumber(0);
        UserService userService = new UserService(gpsMicroserviceProxy, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        userService.addUser(user);
        List<ClosestAttractionDTO> closestAttractionDTOs = userService.getNearByAttractions(userService.getUserLocation("jon"));

        userService.tracker.stopTracking();

        assertEquals(5, closestAttractionDTOs.size());
    }

    @Test
    public void trackUser() throws InterruptedException {
        Locale englishLocale = new Locale("en", "EN");
        Locale.setDefault(englishLocale);
        RewardsService rewardsService = new RewardsService(gpsMicroserviceProxy, rewardsMicroserviceProxy);
        InternalTestHelper.setInternalUserNumber(0);
        UserService userService = new UserService(gpsMicroserviceProxy, rewardsService);

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        CountDownLatch trackLatch = new CountDownLatch( 1 );
        userService.getNewUserLocation(user, trackLatch);
        trackLatch.await();

        userService.tracker.stopTracking();

        assertEquals(user.getUserId(), userService.getLastVisitedLocation(user).userId);
        assertEquals(user.getVisitedLocations().size(), 1);
    }



}

package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.model.DTO.ClosestAttractionDTO;
import tourGuide.user.User;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class GpsService {

    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    
    ExecutorService executorGps = Executors.newFixedThreadPool(150);

    //TODO rename GpsService to LocationService
    //TODO move getAllCurrentLocation from UserService to LocationService
    //TODO documentation

    public GpsService(GpsUtil gpsUtil, RewardsService rewardsService) {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;
    }

    private void updateUser (VisitedLocation loc, User user, CountDownLatch trackLatch) {

//        System.out.println(Thread.currentThread() + " - " + user.getUserName()
//                + " - loc : " + loc.location.longitude + ", " + loc.location.latitude);
        user.addToVisitedLocations(loc);
        rewardsService.calculateRewards(user);
        // one user updated
        trackLatch.countDown();

//        System.out.println(Thread.currentThread() + " - " + user.getUserName()
//                + " - loc : " + loc.location.longitude + ", " + loc.location.latitude);

    }

    public void trackUserLocation(User user, CountDownLatch trackLatch) {

//        LOGGER.debug("visitedLocation to be founded for " + user.getUserName());

          CompletableFuture.supplyAsync(() -> gpsUtil.getUserLocation(user.getUserId()), executorGps)
                    .thenAccept(loc -> updateUser(loc, user, trackLatch));


//                        LOGGER.debug("visitedLocation size for " + user.getUserName() + ", " + user.getVisitedLocations().size());
//                        LOGGER.debug("visitedLocation " + user.getVisitedLocations().get(0).location.latitude + ", "
//                                + user.getVisitedLocations().get(0).location.longitude);

//        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
//        user.addToVisitedLocations(visitedLocation);
//        rewardsService.calculateRewards(user);
//        return visitedLocation;


    }

    public List<ClosestAttractionDTO> getNearByAttractions(VisitedLocation visitedLocation) {
        List<Attraction> attractions = gpsUtil.getAttractions();

//		logger.debug("Visited location : " + visitedLocation.location.longitude + " - " + visitedLocation.location.latitude);
//		for (Attraction attraction : attractions) {
//			logger.debug("Attraction " + attraction.attractionName + " distance : "
//					+ rewardsService.getDistance(new Location(attraction.latitude, attraction.longitude), visitedLocation.location));
//		}

        List<ClosestAttractionDTO> closestAttractionDTOs = attractions.parallelStream()
                //Create a ClosestAttractionDTO form an Attraction, calculating the distance Attraction/User
                .map(attraction -> new ClosestAttractionDTO(attraction.attractionName,
                        new Location(attraction.latitude, attraction.longitude),
                        rewardsService.getDistance(new Location(attraction.latitude, attraction.longitude), visitedLocation.location)
                        ,attraction.attractionId))
                //Sort the ClosestAttractionDTOs from the nearest to the farthest
                .sorted(Comparator.comparing(ClosestAttractionDTO::getDistance))
                //take the 5 nearest
                .limit(5)
                .collect(Collectors.toList());

        closestAttractionDTOs = closestAttractionDTOs.parallelStream()
                .peek(attraction -> {attraction.setVisitedLocation(visitedLocation.location);
                    attraction.setRewardPoints(rewardsService.getRewardCentralPoints(attraction.getAttractionId(), visitedLocation.userId));
                })
                .collect(Collectors.toList());

//		for (ClosestAttractionDTO attraction : closestAttractionDTOs) {
//			attraction.setVisitedLocation(visitedLocation.location);
//			attraction.setRewardPoints(rewardsService.getRewardCentralPoints(attraction.getAttractionId(),
//					visitedLocation.userId));
//			logger.debug("Attraction " + attraction.getAttractionName() + " - "
//						+ attraction.getAttractionLocation().latitude +  " - "
//						+ attraction.getAttractionLocation().longitude +  " - "
//						+ visitedLocation.location.latitude +  " - "
//						+ visitedLocation.location.longitude +  " - "
//						+ attraction.getDistance());
//		}

//		closestAttractionDTOs.forEach( attraction -> {
//					attraction.setVisitedLocation(visitedLocation.location);
//					attraction.setRewardPoints(rewardsService.getRewardCentralPoints(attraction.getAttractionId(),
//							visitedLocation.userId));
//					logger.debug("Attraction " + attraction.getAttractionName() + " - "
//							+ attraction.getAttractionLocation().latitude + " - "
//							+ attraction.getAttractionLocation().longitude + " - "
//							+ visitedLocation.location.latitude + " - "
//							+ visitedLocation.location.longitude + " - "
//							+ attraction.getDistance());
//				}
//		);

        return closestAttractionDTOs;
    }


}

package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import tourGuide.model.DTO.ClosestAttractionDTO;
import tourGuide.user.User;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class GpsService {

    private final GpsUtil gpsUtil;
    private final RewardsService rewardsService;

    ExecutorService executorService = Executors.newFixedThreadPool(2);

    public GpsService(GpsUtil gpsUtil, RewardsService rewardsService) {
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;
    }

    public VisitedLocation trackUserLocation(User user) {
        Locale englishLocale = new Locale("en", "EN");
        Locale.setDefault(englishLocale);

        CompletableFuture<Location> userLocationFuture = new CompletableFuture<>();

        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
        //return userLocationFuture.get();
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
                .map(attraction -> {attraction.setVisitedLocation(visitedLocation.location);
                    attraction.setRewardPoints(rewardsService.getRewardCentralPoints(attraction.getAttractionId(), visitedLocation.userId));
                    return attraction;})
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

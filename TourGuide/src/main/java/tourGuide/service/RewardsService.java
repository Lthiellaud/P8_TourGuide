package tourGuide.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.beans.AttractionBean;
import tourGuide.beans.LocationBean;
import tourGuide.beans.VisitedLocationBean;
import tourGuide.proxies.GpsMicroserviceProxy;
import tourGuide.proxies.RewardsMicroserviceProxy;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RewardsService {
	private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
	private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	@Autowired
	private final GpsMicroserviceProxy gpsMicroserviceProxy;
	@Autowired
	private final RewardsMicroserviceProxy rewardsMicroserviceProxy;

	//TODO improve performances ??

	public RewardsService(GpsMicroserviceProxy gpsMicroserviceProxy, RewardsMicroserviceProxy rewardsMicroserviceProxy) {
		this.gpsMicroserviceProxy = gpsMicroserviceProxy;
		this.rewardsMicroserviceProxy = rewardsMicroserviceProxy;
	}

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	public void calculateRewards(User user) {
		CopyOnWriteArrayList<VisitedLocationBean> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());

		List<AttractionBean> attractions = gpsMicroserviceProxy.getAttractionsList();

		attractions.forEach(attraction -> {
			if(user.getUserRewards().parallelStream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
				userLocations.forEach( visitedLocation -> {
					if(nearAttraction(visitedLocation, attraction)) {
//						CompletableFuture.supplyAsync(() -> getRewardPoints(attraction, user))
//								.thenAccept(points -> user.addUserReward(new UserReward(visitedLocation, attraction, points)));
						user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
					}
				});
			}
		});


//		VisitedLocation lastUserLocation = user.getLastVisitedLocation();
//		List<Attraction> attractions = gpsUtil.getAttractions();
//
//		attractions.forEach(attraction -> {
//			if(user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
//				if(nearAttraction(lastUserLocation, attraction)) {
//					user.addUserReward(new UserReward(lastUserLocation, attraction, getRewardPoints(attraction, user)));
//				}
//			}
//		});

	}

	public int getRewardPoints(AttractionBean attraction, User user) {
		return rewardsMicroserviceProxy.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	public int getRewardCentralPoints(UUID attractionId, UUID userId) {
		return rewardsMicroserviceProxy.getAttractionRewardPoints(attractionId, userId);
	}

	public boolean isWithinAttractionProximity(AttractionBean attraction, LocationBean location) {
		return !(getDistance(attraction, location) > attractionProximityRange);
	}

	private boolean nearAttraction(VisitedLocationBean visitedLocation, AttractionBean attraction) {
		return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
	}

	public double getDistance(LocationBean loc1, LocationBean loc2) {
		double lat1 = Math.toRadians(loc1.latitude);
		double lon1 = Math.toRadians(loc1.longitude);
		double lat2 = Math.toRadians(loc2.latitude);
		double lon2 = Math.toRadians(loc2.longitude);

		double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
				+ Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

		double nauticalMiles = 60 * Math.toDegrees(angle);

		return STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
	}

}

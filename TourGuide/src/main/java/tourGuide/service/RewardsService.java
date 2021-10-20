package tourGuide.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tourGuide.model.beans.AttractionBean;
import tourGuide.model.beans.LocationBean;
import tourGuide.model.beans.VisitedLocationBean;
import tourGuide.proxies.GpsMicroserviceProxy;
import tourGuide.proxies.RewardsMicroserviceProxy;
import tourGuide.model.user.User;
import tourGuide.model.user.UserReward;

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

	/**
	 * Update user rewards from his visited location list
	 * @param user
	 */
	public void calculateRewards(User user) {
		CopyOnWriteArrayList<VisitedLocationBean> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());

		List<AttractionBean> attractions = gpsMicroserviceProxy.getAttractionsList();

		attractions.forEach(attraction -> {
			if(user.getUserRewards().parallelStream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName))) {
				userLocations.forEach( visitedLocation -> {
					if(nearAttraction(visitedLocation, attraction)) {
						user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
					}
				});
			}
		});

	}

	/**
	 * Give the number of reward points associated to a couple Attraction/User
	 * @param attraction
	 * @param user
	 * @return the associated number of reward points
	 */
	public int getRewardPoints(AttractionBean attraction, User user) {
		return getRewardCentralPoints(attraction.attractionId, user.getUserId());
	}

	/**
	 * Give the number of reward points associated to a couple Attraction/User from RewardsCentral
	 * @param attractionId
	 * @param userId
	 * @return
	 */
	public int getRewardCentralPoints(UUID attractionId, UUID userId) {
		return rewardsMicroserviceProxy.getAttractionRewardPoints(attractionId, userId);
	}

	/**
	 * Return true if the distance between attraction and location is smaller than attractionProximityRange
	 * @param attraction
	 * @param location
	 * @return
	 */
	public boolean isWithinAttractionProximity(AttractionBean attraction, LocationBean location) {
		return !(getDistance(attraction, location) > attractionProximityRange);
	}

	/**
	 * Return true if the distance between attraction and visited location is smaller than proximityBuffer
	 * @param attraction
	 * @param visitedLocation
	 * @return
	 */
	private boolean nearAttraction(VisitedLocationBean visitedLocation, AttractionBean attraction) {
		return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
	}

	/**
	 * Calculate the distance between to locations
	 * @param loc1
	 * @param loc2
	 * @return
	 */
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

package tourGuide.model.DTO;

import gpsUtil.location.Location;

import java.util.UUID;

public class ClosestAttractionDTO {
    private String attractionName;
    private Location attractionLocation;
    private Location visitedLocation;
    private double distance;
    private int rewardPoints;
    private transient UUID attractionId;

    public ClosestAttractionDTO(String attractionName, Location attractionLocation, double distance, UUID attractionId) {
        this.attractionName = attractionName;
        this.attractionLocation = attractionLocation;
        this.distance = distance;
        this.attractionId = attractionId;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public Location getAttractionLocation() {
        return attractionLocation;
    }

    public double getDistance() {
        return distance;
    }

    public UUID getAttractionId() {
        return attractionId;
    }

    public void setVisitedLocation(Location visitedLocation) {
        this.visitedLocation = visitedLocation;
    }

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
   }

}

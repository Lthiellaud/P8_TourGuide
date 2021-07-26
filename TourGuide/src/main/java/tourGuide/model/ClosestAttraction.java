package tourGuide.model;

import gpsUtil.location.Location;

import java.util.UUID;

public class ClosestAttraction {
    private String attractionName;
    private Location attractionLocation;
    private double distance;
    private int rewardPoints;
    private UUID attractionId;

    public ClosestAttraction(String attractionName, Location attractionLocation, double distance, UUID attractionId) {
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

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public void setAttractionLocation(Location attractionLocation) {
        this.attractionLocation = attractionLocation;
    }

   public void setDistance(double distance) {
        this.distance = distance;
    }

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }
}

package tourGuide.model.DTO;

import gpsUtil.location.Location;

public class UserCurrentLocationDTO {
    private String userUUID;
    private Location userLocation;

    public UserCurrentLocationDTO(String userUUID, Location userLocation) {
        this.userUUID = userUUID;
        this.userLocation = userLocation;
    }
}

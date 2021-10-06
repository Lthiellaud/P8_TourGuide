package tourGuide.model.DTO;

import tourGuide.model.beans.LocationBean;

public class UserCurrentLocationDTO {
    private String userUUID;
    private LocationBean userLocation;

    public UserCurrentLocationDTO(String userUUID, LocationBean userLocation) {
        this.userUUID = userUUID;
        this.userLocation = userLocation;
    }

    public String getUserUUID() {
        return userUUID;
    }

    public LocationBean getUserLocation() {
        return userLocation;
    }
}

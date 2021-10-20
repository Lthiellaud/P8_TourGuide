package tourGuide.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tourGuide.exception.BadRequestException;
import tourGuide.model.DTO.ClosestAttractionDTO;
import tourGuide.model.DTO.UserCurrentLocationDTO;
import tourGuide.model.DTO.UserPreferencesDTO;
import tourGuide.model.beans.LocationBean;
import tourGuide.model.beans.ProviderBean;
import tourGuide.model.beans.VisitedLocationBean;
import tourGuide.model.user.User;
import tourGuide.model.user.UserReward;
import tourGuide.service.TourGuideService;

import java.util.List;
import java.util.UUID;

@Api("API of TourGuide application")
@RestController
public class TourGuideController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TourGuideService.class);

    @Autowired
    private TourGuideService tourGuideService;

    @ApiOperation(value = "TourGuide home")
    @RequestMapping(method = RequestMethod.GET, value="/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @ApiOperation(value = "Return the location of a user from his username")
    @RequestMapping(method = RequestMethod.GET, value="/getLocation")
    public ResponseEntity<LocationBean> getLocation(@RequestParam String userName) {
        if (userName.equals("")) {
            LOGGER.error("getLocation : userName is mandatory");
            throw new BadRequestException("getLocation : userName is mandatory");
        }

        try {
            VisitedLocationBean visitedLocation = tourGuideService.getUserLocation(userName);
            if (visitedLocation == null) {
                LOGGER.error("getLocation : user not found");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(visitedLocation.location, HttpStatus.OK);
        } catch (InterruptedException e) {
            LOGGER.error("getLocation - Error during retrieving user location");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @ApiOperation(value = "Return the 5 nearest attractions of a user from his username")
    @RequestMapping(method = RequestMethod.GET, value="/getNearbyAttractions")
    public ResponseEntity<List<ClosestAttractionDTO>> getNearbyAttractions(@RequestParam String userName) {
        if (userName.equals("")) {
            LOGGER.error("getNearbyAttractions : userName is mandatory");
            throw new BadRequestException("getNearbyAttractions : userName is mandatory");
        }

        try {
            VisitedLocationBean visitedLocation = tourGuideService.getUserLocation(userName);
            return new ResponseEntity<>(tourGuideService.getNearByAttractions(visitedLocation), HttpStatus.OK);
        } catch (InterruptedException e) {
            LOGGER.error("getNearbyAttractions - Error during retrieving user location");
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @ApiOperation(value = "Return the rewards earned by a user from his username")
    @RequestMapping(method = RequestMethod.GET,value="/getRewards")
    public List<UserReward> getRewards(@RequestParam String userName) {
        if (userName.equals("")) {
            LOGGER.error("getRewards : userName is mandatory");
            throw new BadRequestException("getRewards : userName is mandatory");
        }

        return tourGuideService.getUserRewards(userName);
    }

    @ApiOperation(value = "Return the current location of all connected users")
    @RequestMapping(method = RequestMethod.GET, value="/getAllCurrentLocations")
    public List<UserCurrentLocationDTO> getAllCurrentLocations() {
    	return  tourGuideService.getAllCurrentLocations();
    }

    @ApiOperation(value = "Return for a userName, a list of 5 trips to go to a given " +
            "attraction with the pricing depending on his preferences")
    @RequestMapping(method = RequestMethod.GET, value="/getTripDeals")
    public List<ProviderBean> getTripDeals(@RequestParam String userName, @RequestParam UUID attractionUUID) {
        if (userName.equals("")) {
            LOGGER.error("getTripDeals : userName is mandatory");
            throw new BadRequestException("getTripDeals : userName is mandatory");
        }

        if (attractionUUID.equals("")) {
            LOGGER.error("getTripDeals : attractionUUID is mandatory");
            throw new BadRequestException("getTripDeals : attractionUUID is mandatory");
        }

        return tourGuideService.getTripDeals(userName, attractionUUID);

    }

    @ApiOperation(value = "Update the preferences of a user from his username")
    @RequestMapping(method = RequestMethod.PUT, value = "/userPreferences")
    public User updateUserPreference(@RequestParam String userName, @RequestBody UserPreferencesDTO userPreferences) {
        if (userName.equals("")) {
            LOGGER.error("PUT UserPreferences : userName is mandatory");
            throw new BadRequestException("PUT UserPreferences : userName is mandatory");
        }

        return tourGuideService.updateUserPreferences(userName, userPreferences);

    }

    @ApiOperation(value = "Return the preferences of a user from his username")
    @RequestMapping(method = RequestMethod.GET, value = "/userPreferences")
    public UserPreferencesDTO getUserPreference(@RequestParam String userName) {
        if (userName.equals("")) {
            LOGGER.error("GET UserPreferences : userName is mandatory");
            throw new BadRequestException("GET UserPreferences : userName is mandatory");
        }

        return tourGuideService.getUserPreferences(userName);
    }

}
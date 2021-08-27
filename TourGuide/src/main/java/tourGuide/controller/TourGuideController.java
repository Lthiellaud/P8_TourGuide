package tourGuide.controller;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tourGuide.model.DTO.ClosestAttractionDTO;
import tourGuide.model.DTO.UserCurrentLocationDTO;
import tourGuide.model.DTO.UserPreferencesDTO;
import tourGuide.service.GpsService;
import tourGuide.service.UserService;
import tourGuide.user.User;
import tourGuide.user.UserReward;
import tripPricer.Provider;

import javax.money.UnknownCurrencyException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class TourGuideController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    UserService userService;

    @Autowired
    GpsService gpsService;

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    @RequestMapping("/getLocation") 
    public ResponseEntity<Location> getLocation(@RequestParam String userName) {
        if (userName.equals("")) {
            LOGGER.error("getLocation : userName is mandatory");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            VisitedLocation visitedLocation = userService.getUserLocation(userName);
            return new ResponseEntity<>(visitedLocation.location, HttpStatus.OK);
        } catch (InterruptedException e) {
            LOGGER.error("getLocation - Error during retrieving user location");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }
    
    @RequestMapping("/getNearbyAttractions")
    public ResponseEntity<List<ClosestAttractionDTO>> getNearbyAttractions(@RequestParam String userName) {
        if (userName.equals("")) {
            LOGGER.error("getNearbyAttractions : userName is mandatory");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            VisitedLocation visitedLocation = userService.getUserLocation(userName);
            return new ResponseEntity<>(gpsService.getNearByAttractions(visitedLocation), HttpStatus.OK);
        } catch (InterruptedException e) {
            LOGGER.error("getNearbyAttractions - Error during retrieving user location");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }
    
    @RequestMapping("/getRewards") 
    public ResponseEntity<List<UserReward>> getRewards(@RequestParam String userName) {
        if (userName.equals("")) {
            LOGGER.error("getRewards : userName is mandatory");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(userService.getUserRewards(userName), HttpStatus.OK);
    }
    
    @RequestMapping("/getAllCurrentLocations")
    public ResponseEntity<List<UserCurrentLocationDTO>> getAllCurrentLocations() {
    	return  new ResponseEntity<>(userService.getAllCurrentLocations(), HttpStatus.OK);
    }

    @RequestMapping("/getTripDeals")
    public ResponseEntity<List<Provider>> getTripDeals(@RequestParam String userName, @RequestParam String attractionName) {
        if (userName.equals("")) {
            LOGGER.error("getTripDeals : userName is mandatory");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (attractionName.equals("")) {
            LOGGER.error("getTripDeals : attractionName is mandatory");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<Provider> providers = userService.getTripDeals(userName, attractionName);
    	return new ResponseEntity<>(providers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/userPreferences")
    public ResponseEntity<User> updateUserPreference(@RequestParam String userName, @RequestBody UserPreferencesDTO userPreferences) {
        if (userName.equals("")) {
            LOGGER.error("PUT UserPreferences : userName is mandatory");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            User user = userService.updateUserPreferences(userName, userPreferences);
            if (user == null) {
                LOGGER.error("PUT UserPreferences : userName does not exist");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (UnknownCurrencyException e) {
            LOGGER.error("PUT UserPreferences : Problem while updating currency");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/userPreferences")
    public ResponseEntity<UserPreferencesDTO> getUserPreference(@RequestParam String userName) {
        if (userName.equals("")) {
            LOGGER.error("GET UserPreferences : userName is mandatory");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        UserPreferencesDTO userPreferences = userService.getUserPreferences(userName);
        if (userPreferences.getAttractionProximity() == -1) {
            LOGGER.error("GET UserPreferences : userName does not exist");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(userPreferences, HttpStatus.OK);
    }

}
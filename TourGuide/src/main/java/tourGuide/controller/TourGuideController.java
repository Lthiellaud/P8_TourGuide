package tourGuide.controller;

import java.util.List;
import java.util.concurrent.ExecutionException;

import gpsUtil.location.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.model.DTO.ClosestAttractionDTO;
import tourGuide.model.DTO.UserCurrentLocationDTO;
import tourGuide.service.GpsService;
import tourGuide.service.UserService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserReward;
import tripPricer.Provider;

@RestController
public class TourGuideController {

    private Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    UserService userService;

    @Autowired
    GpsService gpsService;

    //  TODO: Passer en ResponseEntity.

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    @RequestMapping("/getLocation") 
    public ResponseEntity<Location> getLocation(@RequestParam String userName) {
        try {
            VisitedLocation visitedLocation = userService.getUserLocation(getUser(userName));
            return new ResponseEntity<>(visitedLocation.location, HttpStatus.OK);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    
    @RequestMapping("/getNearbyAttractions")
    public ResponseEntity<List<ClosestAttractionDTO>> getNearbyAttractions(@RequestParam String userName) {

        try {
            VisitedLocation visitedLocation = userService.getUserLocation(getUser(userName));
            return new ResponseEntity<>(gpsService.getNearByAttractions(visitedLocation), HttpStatus.OK);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
    
    @RequestMapping("/getRewards") 
    public ResponseEntity<List<UserReward>> getRewards(@RequestParam String userName) {
    	return new ResponseEntity<>(userService.getUserRewards(getUser(userName)), HttpStatus.OK);
    }
    
    @RequestMapping("/getAllCurrentLocations")
    public ResponseEntity<List<UserCurrentLocationDTO>> getAllCurrentLocations() {
    	return  new ResponseEntity<>(userService.getAllCurrentLocations(), HttpStatus.OK);
    }

    @RequestMapping("/getTripDeals")
    public ResponseEntity<List<Provider>> getTripDeals(@RequestParam String userName) {
        //TODO Corriger pour prendre en compte l'attraction
        List<Provider> providers = userService.getTripDeals(getUser(userName));
    	return new ResponseEntity<>(providers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/userPreferences")
    public ResponseEntity<User> updateUserPreference(@RequestParam String userName, @RequestBody UserPreferences userPreferences) {
        if (userName.equals("")) {
            logger.error("PUT UserPreferences : userName is mandatory");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (getUser(userName) == null) {
            logger.error("PUT UserPreferences : userName does not exist");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }


        return new ResponseEntity<>(userService.getUser(userName), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/userPreferences")
    public ResponseEntity<UserPreferences> getUserPreference(@RequestParam String userName) {
        if (userName.equals("")) {
            logger.error("GET UserPreferences : userName is mandatory");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (getUser(userName) == null) {
            logger.error("GET UserPreferences : userName does not exist");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(getUser(userName).getUserPreferences(), HttpStatus.OK);
    }

    private User getUser(String userName) {
    	return userService.getUser(userName);
    }



}
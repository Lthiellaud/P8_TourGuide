package tourGuide.controller;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tripPricer.Provider;

@RestController
public class TourGuideController {

    private Logger logger = LoggerFactory.getLogger(TourGuideService.class);

    @Autowired
	TourGuideService tourGuideService;

	//  TODO: Passer en ResponseEntity.

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    @RequestMapping("/getLocation") 
    public String getLocation(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
		return JsonStream.serialize(visitedLocation.location);
    }
    
    @RequestMapping("/getNearbyAttractions")
    public String getNearbyAttractions(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
    	return JsonStream.serialize(tourGuideService.getNearByAttractions(visitedLocation));
    }
    
    @RequestMapping("/getRewards") 
    public String getRewards(@RequestParam String userName) {
    	return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
    }
    
    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
    	return JsonStream.serialize(tourGuideService.getAllCurrentLocations());
    }

    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) {
        //TODO Corriger pour prendre en compte l'attraction
        List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
    	return JsonStream.serialize(providers);
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

        return new ResponseEntity<>(tourGuideService.getUser(userName), HttpStatus.OK);
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
    	return tourGuideService.getUser(userName);
    }



}
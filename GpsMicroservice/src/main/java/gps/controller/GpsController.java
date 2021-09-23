package gps.controller;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestParam;



@RestController
public class GpsController {

    GpsUtil gpsUtil = new GpsUtil();

    @GetMapping(value="/AttractionsList")
    public List<Attraction> getAttractionsList() {
        return gpsUtil.getAttractions();
    }
    
    @GetMapping(value="/UserLocation")
    public VisitedLocation getUserLocation (@RequestParam UUID userId) {
        return gpsUtil.getUserLocation(userId);
    }
    
}

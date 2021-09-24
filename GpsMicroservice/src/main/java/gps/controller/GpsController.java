package gps.controller;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestParam;


@Api("API of GPS Microservice")
@RestController
public class GpsController {

    GpsUtil gpsUtil = new GpsUtil();

    @ApiOperation(value = "List of all attractions known in TourGuide")
    @GetMapping(value="/AttractionsList")
    public List<Attraction> getAttractionsList() {
        return gpsUtil.getAttractions();
    }

    @ApiOperation(value = "To get user Location")
    @GetMapping(value="/UserLocation")
    public VisitedLocation getUserLocation (@RequestParam UUID userId) {
        return gpsUtil.getUserLocation(userId);
    }
    
}

package gps.controller;

import gps.exception.NotFoundException;
import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.RequestParam;


@Api("API of GPS Microservice")
@RestController
public class GpsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GpsController.class);

    GpsUtil gpsUtil = new GpsUtil();

    @ApiOperation(value = "List of all attractions known in TourGuide")
    @GetMapping(value="/AttractionsList")
    public List<Attraction> getAttractionsList() {
        LOGGER.debug("GpsUtil - Attractions list sent");
        List<Attraction> attractions = gpsUtil.getAttractions();
        if (attractions == null || attractions.isEmpty()) {
            throw new NotFoundException("Attraction List not obtained");
        }
        return attractions;
    }

    @ApiOperation(value = "To get user Location")
    @GetMapping(value="/UserLocation")
    public VisitedLocation getUserLocation (@RequestParam UUID userId) {
        LOGGER.debug("GpsUtil call for " + userId);
        VisitedLocation visitedLocation = gpsUtil.getUserLocation(userId);
        if (visitedLocation == null) {
            throw new NotFoundException("User location not obtained");
        }
        return visitedLocation;
    }
    
}

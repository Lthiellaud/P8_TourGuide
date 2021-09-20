package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.model.DTO.ClosestAttractionDTO;
import tourGuide.user.User;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class GpsService {

    private final GpsUtil gpsUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(GpsService.class);
    
    ExecutorService executorGps = Executors.newFixedThreadPool(125);

    //TODO rename GpsService to LocationService
    //TODO move getAllCurrentLocation from UserService to LocationService
    //TODO documentation

    public GpsService(GpsUtil gpsUtil) {
        this.gpsUtil = gpsUtil;

    }


    public List<Attraction> getAttractionsList() {
        return gpsUtil.getAttractions();
    }

    public VisitedLocation getUserLocation(User user) {
        return gpsUtil.getUserLocation(user.getUserId());
    }



}

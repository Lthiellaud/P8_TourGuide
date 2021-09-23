package trippricer.controller;

import org.springframework.web.bind.annotation.RestController;
import tripPricer.Provider;
import tripPricer.TripPricer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;


@RestController
public class TripPricerController {
    
    TripPricer tripPricer = new TripPricer();
    
    @GetMapping(value="/tripPrice")
    public List<Provider> getPrice(@RequestParam String apiKey,
                                   @RequestParam UUID attractionId,
                                   @RequestParam int adults,
                                   @RequestParam int children,
                                   @RequestParam int nightsStay,
                                   @RequestParam int rewardsPoints){
        return tripPricer.getPrice(apiKey, attractionId, adults,children, nightsStay, rewardsPoints);
    }
    
}

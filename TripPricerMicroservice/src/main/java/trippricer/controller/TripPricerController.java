package trippricer.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import tripPricer.Provider;
import tripPricer.TripPricer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import trippricer.exception.NotFoundException;

import java.util.List;
import java.util.UUID;


@Api("API of Trip Pricer Microservice")
@RestController
public class TripPricerController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TripPricerController.class);

    TripPricer tripPricer = new TripPricer();

    @ApiOperation(value = "Return a list of provider for an attraction and the user's preferences")
    @GetMapping(value="/tripPrice")
    public List<Provider> getProviderList(@RequestParam String apiKey,
                                          @RequestParam UUID attractionId,
                                          @RequestParam int adults,
                                          @RequestParam int children,
                                          @RequestParam int nightsStay,
                                          @RequestParam int rewardsPoints){
        LOGGER.debug("tripPricer call for " + attractionId);
        List<Provider> providers = tripPricer.getPrice(apiKey, attractionId, adults,children, nightsStay, rewardsPoints);
        if (providers == null || providers.isEmpty()) {
            throw new NotFoundException("Provider List not obtained");
        }
        return providers;
    }
    
}

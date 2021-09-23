package tourGuide.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tripPricer.Provider;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "TripPricer", url = "localhost:9093")
public interface TripPricerMicroserviceProxy {

    @GetMapping(value="/tripPrice")
    List<Provider> getPrice(@RequestParam String apiKey,
                                   @RequestParam ("attractionId") UUID attractionId,
                                   @RequestParam ("adults") int adults,
                                   @RequestParam ("children") int children,
                                   @RequestParam ("nightsStay") int nightsStay,
                                   @RequestParam ("rewardsPoints") int rewardsPoints);
}

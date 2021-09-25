package tourGuide.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tourGuide.beans.AttractionBean;
import tourGuide.beans.VisitedLocationBean;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "RewardsMicroservice", url = "${feign.url.rewards}")
public interface RewardsMicroserviceProxy {

    @GetMapping(value="/userRewardsPoint")
    Integer getAttractionRewardPoints (@RequestParam("userId") UUID userId, @RequestParam("attractionId") UUID attractionId);
}

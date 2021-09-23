package rewards.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rewardCentral.RewardCentral;

import java.util.UUID;

@RestController
public class RewardsController {

    RewardCentral rewardCentral = new RewardCentral();

    @GetMapping(value="/userRewardsPoint")
    public Integer getAttractionRewardPoints (@RequestParam UUID userId, @RequestParam UUID attractionId) {
        return rewardCentral.getAttractionRewardPoints(attractionId, userId);
    }
}

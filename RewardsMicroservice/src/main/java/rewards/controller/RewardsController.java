package rewards.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rewardCentral.RewardCentral;

import java.util.UUID;

@Api("API of Rewards Microservice")
@RestController
public class RewardsController {

    RewardCentral rewardCentral = new RewardCentral();

    @ApiOperation(value = "to get the number of reward points associated to an attraction")
    @GetMapping(value="/userRewardsPoint")
    public Integer getAttractionRewardPoints (@RequestParam UUID userId, @RequestParam UUID attractionId) {
        return rewardCentral.getAttractionRewardPoints(attractionId, userId);
    }
}

package rewards.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rewardCentral.RewardCentral;
import rewards.exception.RewardsNotObtainedException;

import java.util.UUID;

@Api("API of Rewards Microservice")
@RestController
public class RewardsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RewardsController.class);

    RewardCentral rewardCentral = new RewardCentral();

    @ApiOperation(value = "to get the number of reward points associated to an attraction")
    @GetMapping(value="/userRewardsPoint")
    public Integer getAttractionRewardPoints (@RequestParam UUID userId, @RequestParam UUID attractionId) {
        LOGGER.debug("rewardCentral call for " + userId + " for attraction "+ attractionId);
        Integer rewards = rewardCentral.getAttractionRewardPoints(attractionId, userId);
        if (rewards == null) {
            throw new RewardsNotObtainedException("RewardsMicroserice - Rewards not obtained");
        }
        return rewards;
    }
}

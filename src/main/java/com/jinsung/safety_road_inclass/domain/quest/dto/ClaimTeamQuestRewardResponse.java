package com.jinsung.safety_road_inclass.domain.quest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClaimTeamQuestRewardResponse {

    private final Long questId;
    private final String code;
    private final String periodKey;
    private final int rewardedMembers;
    private final TeamQuestResponse.Reward reward;
}

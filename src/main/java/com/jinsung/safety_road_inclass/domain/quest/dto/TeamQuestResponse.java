package com.jinsung.safety_road_inclass.domain.quest.dto;

import com.jinsung.safety_road_inclass.domain.quest.entity.QuestConditionType;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TeamQuestResponse {

    private final Long id;
    private final String code;
    private final String title;
    private final String description;
    private final QuestType type;
    private final QuestConditionType conditionType;
    private final String periodKey;
    private final int completedMemberCount;
    private final int totalMemberCount;
    private final boolean completed;
    private final boolean rewardClaimed;
    private final Reward reward;
    private final List<TeamQuestMemberProgressResponse> members;

    @Getter
    @Builder
    public static class Reward {
        private final int points;
        private final int exp;
        private final int gold;
    }
}

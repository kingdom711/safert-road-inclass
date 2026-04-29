package com.jinsung.safety_road_inclass.domain.quest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamQuestSummaryResponse {

    private final int weeklyProgress;
    private final int completedQuests;
    private final int totalQuests;
}

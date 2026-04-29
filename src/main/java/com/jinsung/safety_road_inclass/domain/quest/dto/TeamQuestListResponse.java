package com.jinsung.safety_road_inclass.domain.quest.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TeamQuestListResponse {

    private final String code;
    private final List<TeamQuestResponse> quests;
}

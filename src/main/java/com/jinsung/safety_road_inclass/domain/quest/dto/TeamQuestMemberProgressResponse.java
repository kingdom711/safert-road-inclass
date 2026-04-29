package com.jinsung.safety_road_inclass.domain.quest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamQuestMemberProgressResponse {

    private final Long userId;
    private final String name;
    private final boolean me;
    private final boolean completed;
}

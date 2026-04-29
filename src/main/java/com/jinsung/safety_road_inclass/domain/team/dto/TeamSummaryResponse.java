package com.jinsung.safety_road_inclass.domain.team.dto;

import com.jinsung.safety_road_inclass.domain.team.entity.Team;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamSummaryResponse {

    private final Long id;
    private final String name;
    private final String siteName;
    private final Long leaderUserId;
    private final String leaderName;
    private final long activeMemberCount;

    public static TeamSummaryResponse of(Team team, long activeMemberCount) {
        return TeamSummaryResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .siteName(team.getSiteName())
                .leaderUserId(team.getLeader().getId())
                .leaderName(team.getLeader().getName())
                .activeMemberCount(activeMemberCount)
                .build();
    }
}

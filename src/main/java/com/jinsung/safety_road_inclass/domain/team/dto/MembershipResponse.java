package com.jinsung.safety_road_inclass.domain.team.dto;

import com.jinsung.safety_road_inclass.domain.team.entity.MembershipStatus;
import com.jinsung.safety_road_inclass.domain.team.entity.TeamMember;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MembershipResponse {

    private final Long membershipId;
    private final Long teamId;
    private final String teamName;
    private final Long userId;
    private final MembershipStatus status;

    public static MembershipResponse of(TeamMember member) {
        return MembershipResponse.builder()
                .membershipId(member.getId())
                .teamId(member.getTeam().getId())
                .teamName(member.getTeam().getName())
                .userId(member.getUser().getId())
                .status(member.getStatus())
                .build();
    }
}

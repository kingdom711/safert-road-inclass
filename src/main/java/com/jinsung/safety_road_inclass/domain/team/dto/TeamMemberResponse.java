package com.jinsung.safety_road_inclass.domain.team.dto;

import com.jinsung.safety_road_inclass.domain.team.entity.MembershipStatus;
import com.jinsung.safety_road_inclass.domain.team.entity.TeamMember;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TeamMemberResponse {

    private final Long membershipId;
    private final Long userId;
    private final String userName;
    private final String username;
    private final MembershipStatus status;
    private final boolean leader;
    private final LocalDateTime joinedAt;
    private final LocalDateTime approvedAt;

    public static TeamMemberResponse of(TeamMember member) {
        return TeamMemberResponse.builder()
                .membershipId(member.getId())
                .userId(member.getUser().getId())
                .userName(member.getUser().getName())
                .username(member.getUser().getUsername())
                .status(member.getStatus())
                .leader(member.getTeam().getLeader() != null
                        && member.getTeam().getLeader().getId().equals(member.getUser().getId()))
                .joinedAt(member.getJoinedAt())
                .approvedAt(member.getApprovedAt())
                .build();
    }
}

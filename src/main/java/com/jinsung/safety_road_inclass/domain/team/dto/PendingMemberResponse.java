package com.jinsung.safety_road_inclass.domain.team.dto;

import com.jinsung.safety_road_inclass.domain.team.entity.TeamMember;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PendingMemberResponse {

    private final Long membershipId;
    private final Long userId;
    private final String userName;
    private final String username;
    private final LocalDateTime requestedAt;

    public static PendingMemberResponse of(TeamMember member) {
        return PendingMemberResponse.builder()
                .membershipId(member.getId())
                .userId(member.getUser().getId())
                .userName(member.getUser().getName())
                .username(member.getUser().getUsername())
                .requestedAt(member.getJoinedAt())
                .build();
    }
}

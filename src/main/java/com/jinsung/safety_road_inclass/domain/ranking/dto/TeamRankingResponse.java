package com.jinsung.safety_road_inclass.domain.ranking.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * TeamRankingResponse - 팀(역할 그룹) 랭킹 응답 DTO
 *
 * 현재 팀/부서 엔티티가 없으므로 역할(Role) 기반으로 그룹을 생성한다.
 * 향후 Team 엔티티 추가 시 확장 가능.
 */
@Getter
@Builder
public class TeamRankingResponse {

    private int rank;
    private Long teamId;
    private String teamName;
    private int memberCount;
    private long totalPoints;
    private int avgPoints;
    private String topMember;

    public static TeamRankingResponse of(int rank, String teamName, int memberCount,
                                          long totalPoints, int avgPoints, String topMember) {
        return of(rank, null, teamName, memberCount, totalPoints, avgPoints, topMember);
    }

    public static TeamRankingResponse of(int rank, Long teamId, String teamName, int memberCount,
                                          long totalPoints, int avgPoints, String topMember) {
        return TeamRankingResponse.builder()
                .rank(rank)
                .teamId(teamId)
                .teamName(teamName)
                .memberCount(memberCount)
                .totalPoints(totalPoints)
                .avgPoints(avgPoints)
                .topMember(topMember)
                .build();
    }
}

package com.jinsung.safety_road_inclass.domain.ranking.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * RankingEntryResponse - 개인 랭킹 엔트리 응답 DTO
 */
@Getter
@Builder
public class RankingEntryResponse {

    private int rank;
    private Long userId;
    private String name;
    private String role;       // WORKER, SUPERVISOR, SAFETY_MANAGER
    private int points;
    private int level;
    private int streak;

    public static RankingEntryResponse of(int rank, Long userId, String name, String role,
                                           int points, int level, int streak) {
        return RankingEntryResponse.builder()
                .rank(rank)
                .userId(userId)
                .name(name)
                .role(role)
                .points(points)
                .level(level)
                .streak(streak)
                .build();
    }
}

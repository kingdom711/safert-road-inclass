package com.jinsung.safety_road_inclass.domain.ranking.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * MyRankResponse - 현재 사용자 랭킹 응답 DTO
 */
@Getter
@Builder
public class MyRankResponse {

    private int rank;
    private int totalUsers;
    private Long userId;
    private String name;
    private String role;
    private int points;
    private int level;
    private int streak;

    public static MyRankResponse of(int rank, int totalUsers, Long userId, String name, String role,
                                     int points, int level, int streak) {
        return MyRankResponse.builder()
                .rank(rank)
                .totalUsers(totalUsers)
                .userId(userId)
                .name(name)
                .role(role)
                .points(points)
                .level(level)
                .streak(streak)
                .build();
    }
}

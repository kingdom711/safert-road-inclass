package com.jinsung.safety_road_inclass.domain.gameprofile.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

/**
 * FullGameDataResponse - 전체 게임 데이터 통합 응답 DTO
 * 크로스 디바이스 동기화를 위해 로그인 시 한 번에 모든 게임 데이터를 반환
 */
@Getter
@Builder
public class FullGameDataResponse {
    private GameProfileResponse profile;
    private List<SpecializationResponse> specializations;
    private PointsSummary points;
    private StreakSummary streak;

    @Getter
    @Builder
    public static class PointsSummary {
        private int balance;
        private int totalEarned;
        private int totalSpent;
    }

    @Getter
    @Builder
    public static class StreakSummary {
        private int currentStreak;
        private int longestStreak;
        private LocalDate lastCheckInDate;
    }
}

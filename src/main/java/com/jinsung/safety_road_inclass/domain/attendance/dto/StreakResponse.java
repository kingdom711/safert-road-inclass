package com.jinsung.safety_road_inclass.domain.attendance.dto;

import com.jinsung.safety_road_inclass.domain.attendance.entity.UserStreak;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * StreakResponse - 스트릭 조회 응답 DTO
 */
@Getter
@Builder
public class StreakResponse {

    private int currentStreak;
    private int longestStreak;
    private LocalDate lastCheckInDate;

    public static StreakResponse from(UserStreak userStreak) {
        return StreakResponse.builder()
                .currentStreak(userStreak.getCurrentStreak())
                .longestStreak(userStreak.getLongestStreak())
                .lastCheckInDate(userStreak.getLastCheckInDate())
                .build();
    }

    public static StreakResponse empty() {
        return StreakResponse.builder()
                .currentStreak(0)
                .longestStreak(0)
                .lastCheckInDate(null)
                .build();
    }
}

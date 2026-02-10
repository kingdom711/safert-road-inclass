package com.jinsung.safety_road_inclass.domain.attendance.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * CheckInResponse - 출석 체크인 응답 DTO
 */
@Getter
@Builder
public class CheckInResponse {

    private LocalDate checkInDate;
    private int pointsEarned;
    private int currentStreak;
    private int longestStreak;
    private boolean bonusAwarded;
}

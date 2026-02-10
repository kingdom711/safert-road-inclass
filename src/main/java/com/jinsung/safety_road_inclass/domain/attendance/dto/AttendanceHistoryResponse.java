package com.jinsung.safety_road_inclass.domain.attendance.dto;

import com.jinsung.safety_road_inclass.domain.attendance.entity.AttendanceRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * AttendanceHistoryResponse - 출석 달력 데이터 응답 DTO
 */
@Getter
@Builder
public class AttendanceHistoryResponse {

    private LocalDate checkInDate;
    private int pointsEarned;
    private int streakAtTime;

    public static AttendanceHistoryResponse from(AttendanceRecord record) {
        return AttendanceHistoryResponse.builder()
                .checkInDate(record.getCheckInDate())
                .pointsEarned(record.getPointsEarned())
                .streakAtTime(record.getStreakAtTime())
                .build();
    }
}

package com.jinsung.safety_road_inclass.domain.attendance.controller;

import com.jinsung.safety_road_inclass.domain.attendance.dto.AttendanceHistoryResponse;
import com.jinsung.safety_road_inclass.domain.attendance.dto.CheckInResponse;
import com.jinsung.safety_road_inclass.domain.attendance.dto.StreakResponse;
import com.jinsung.safety_road_inclass.domain.attendance.service.AttendanceService;
import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * AttendanceController - 출석/스트릭 API
 */
@Tag(name = "Attendance", description = "출석 체크인 및 스트릭 API")
@RestController
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "출석 체크인", description = "오늘 출석 체크인을 합니다. 포인트가 자동으로 적립됩니다.")
    @PostMapping("/api/v1/quests/attendance/check-in")
    public ApiResponse<CheckInResponse> checkIn(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ApiResponse.success(attendanceService.checkIn(principal.userId()));
    }

    @Operation(summary = "출석 달력 데이터 조회", description = "기간별 출석 기록을 조회합니다. 달력 표시용.")
    @GetMapping("/api/v1/quests/attendance/history")
    public ApiResponse<List<AttendanceHistoryResponse>> getHistory(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ApiResponse.success(attendanceService.getHistory(principal.userId(), startDate, endDate));
    }

    @Operation(summary = "스트릭 조회", description = "현재 로그인한 유저의 출석 스트릭을 조회합니다.")
    @GetMapping("/api/v1/users/me/streak")
    public ApiResponse<StreakResponse> getStreak(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ApiResponse.success(attendanceService.getStreak(principal.userId()));
    }
}

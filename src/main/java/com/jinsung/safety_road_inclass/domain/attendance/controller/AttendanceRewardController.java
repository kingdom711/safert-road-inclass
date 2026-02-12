package com.jinsung.safety_road_inclass.domain.attendance.controller;

import com.jinsung.safety_road_inclass.domain.attendance.dto.AvailableRewardResponse;
import com.jinsung.safety_road_inclass.domain.attendance.dto.RewardClaimedResponse;
import com.jinsung.safety_road_inclass.domain.attendance.service.AttendanceRewardService;
import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AttendanceRewardController - 출석 보상 수령 API
 */
@Tag(name = "Attendance Reward", description = "출석 보상 수령 API")
@RestController
@RequestMapping("/api/v1/attendance/rewards")
@RequiredArgsConstructor
public class AttendanceRewardController {

    private final AttendanceRewardService attendanceRewardService;

    @Operation(summary = "수령 가능한 보상 목록 조회", description = "특정 년월의 수령 가능한 출석 보상을 조회합니다.")
    @GetMapping("/available")
    public ApiResponse<List<AvailableRewardResponse>> getAvailableRewards(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam int year,
            @RequestParam int month) {

        List<AvailableRewardResponse> rewards = attendanceRewardService.getAvailableRewards(
                principal.userId(), year, month);

        return ApiResponse.success(rewards);
    }

    @Operation(summary = "보상 수령", description = "출석 보상을 수령하여 인벤토리에 추가합니다.")
    @PostMapping("/{rewardId}/claim")
    public ApiResponse<RewardClaimedResponse> claimReward(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long rewardId) {

        RewardClaimedResponse response = attendanceRewardService.claimReward(
                principal.userId(), rewardId);

        return ApiResponse.success(response);
    }
}

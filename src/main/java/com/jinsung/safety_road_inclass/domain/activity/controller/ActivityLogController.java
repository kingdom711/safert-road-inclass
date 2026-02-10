package com.jinsung.safety_road_inclass.domain.activity.controller;

import com.jinsung.safety_road_inclass.domain.activity.dto.ActivityLogResponse;
import com.jinsung.safety_road_inclass.domain.activity.service.ActivityLogService;
import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ActivityLogController - 활동 기록 API
 */
@Tag(name = "Activity", description = "활동 기록 API")
@RestController
@RequestMapping("/api/v1/users/me/activities")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @Operation(summary = "활동 기록 조회", description = "현재 로그인한 유저의 활동 기록을 페이징 조회합니다.")
    @GetMapping
    public ApiResponse<Page<ActivityLogResponse>> getActivities(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.success(activityLogService.getActivities(principal.userId(), pageable));
    }
}

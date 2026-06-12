package com.jinsung.safety_road_inclass.domain.admin.controller;

import com.jinsung.safety_road_inclass.domain.admin.dto.AdminDashboardSummaryResponse;
import com.jinsung.safety_road_inclass.domain.admin.dto.ParticipantEngagementResponse;
import com.jinsung.safety_road_inclass.domain.admin.dto.PointRewardDashboardResponse;
import com.jinsung.safety_road_inclass.domain.admin.service.AdminDashboardService;
import com.jinsung.safety_road_inclass.domain.admin.service.AdminParticipantEngagementService;
import com.jinsung.safety_road_inclass.domain.admin.service.AdminPointRewardDashboardService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final AdminParticipantEngagementService adminParticipantEngagementService;
    private final AdminPointRewardDashboardService adminPointRewardDashboardService;

    @GetMapping("/summary")
    public ApiResponse<AdminDashboardSummaryResponse> getSummary() {
        return ApiResponse.success(adminDashboardService.getSummary());
    }

    @GetMapping("/participants")
    public ApiResponse<ParticipantEngagementResponse> getParticipantEngagement(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "score") String sort
    ) {
        return ApiResponse.success(adminParticipantEngagementService.getParticipantEngagement(from, to, teamId, keyword, sort));
    }

    @GetMapping("/points")
    public ApiResponse<PointRewardDashboardResponse> getPointRewardDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "balance") String sort
    ) {
        return ApiResponse.success(adminPointRewardDashboardService.getPointRewardDashboard(from, to, teamId, keyword, sort));
    }
}

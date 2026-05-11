package com.jinsung.safety_road_inclass.domain.admin.controller;

import com.jinsung.safety_road_inclass.domain.admin.dto.AdminDashboardSummaryResponse;
import com.jinsung.safety_road_inclass.domain.admin.service.AdminDashboardService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    public ApiResponse<AdminDashboardSummaryResponse> getSummary() {
        return ApiResponse.success(adminDashboardService.getSummary());
    }
}

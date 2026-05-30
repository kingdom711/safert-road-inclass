package com.jinsung.safety_road_inclass.domain.report.controller;

import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import com.jinsung.safety_road_inclass.domain.report.dto.UserReportResponse;
import com.jinsung.safety_road_inclass.domain.report.service.UserReportService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Report", description = "사용자 성장 리포트 API")
@RestController
@RequestMapping("/api/v1/users/me/reports")
@RequiredArgsConstructor
public class ReportController {

    private final UserReportService userReportService;

    @Operation(summary = "내 성장 리포트 조회", description = "주간, 월간, 연간 사용자 활동 리포트를 생성합니다.")
    @GetMapping
    @PreAuthorize("hasRole('PROJECT_ADMIN')")
    public ApiResponse<UserReportResponse> getReport(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(value = "type", defaultValue = "weekly") String type) {
        return ApiResponse.success(userReportService.generate(principal.userId(), type));
    }
}

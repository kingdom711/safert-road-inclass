package com.jinsung.safety_road_inclass.domain.compliance.controller;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.compliance.dto.ComplianceReportResponse;
import com.jinsung.safety_road_inclass.domain.compliance.service.ComplianceReportService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ComplianceReportController - 컴플라이언스 리포트 REST API
 *
 * 중대재해처벌법 증거자료 관리:
 * - 월간 안전관리 종합 리포트를 자동 생성하여 DB에 영구 보존
 * - 고용노동부 점검 시 즉시 제출 가능한 형태로 저장
 * - SUPERVISOR / SAFETY_MANAGER / ADMIN 권한 필요
 */
@Tag(name = "Compliance", description = "컴플라이언스 리포트 API (중대재해처벌법 증거자료)")
@RestController
@RequestMapping("/api/v1/compliance-reports")
@RequiredArgsConstructor
@Slf4j
public class ComplianceReportController {

    private final ComplianceReportService complianceReportService;

    @Operation(summary = "월간 컴플라이언스 리포트 생성",
               description = "해당 연월의 교육·위험성평가·작업중지·체크리스트·사고 데이터를 종합하여 리포트를 생성합니다. 이미 생성된 리포트가 있으면 기존 리포트를 반환합니다.")
    @PostMapping("/generate")
    public ApiResponse<ComplianceReportResponse> generateReport(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
            @RequestParam int year,
            @RequestParam int month) {
        return ApiResponse.success(complianceReportService.generateMonthlyReport(year, month, currentUser));
    }

    @Operation(summary = "특정 연월 리포트 조회")
    @GetMapping
    public ApiResponse<ComplianceReportResponse> getReport(
            @RequestParam int year,
            @RequestParam int month) {
        return ApiResponse.success(complianceReportService.getReport(year, month));
    }

    @Operation(summary = "전체 리포트 목록 조회", description = "생성된 모든 컴플라이언스 리포트를 최신순으로 반환합니다.")
    @GetMapping("/all")
    public ApiResponse<List<ComplianceReportResponse>> getAllReports() {
        return ApiResponse.success(complianceReportService.getAllReports());
    }

    @Operation(summary = "연도별 리포트 목록 조회")
    @GetMapping("/year/{year}")
    public ApiResponse<List<ComplianceReportResponse>> getReportsByYear(@PathVariable int year) {
        return ApiResponse.success(complianceReportService.getReportsByYear(year));
    }
}

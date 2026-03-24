package com.jinsung.safety_road_inclass.domain.workstop.controller;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.workstop.dto.*;
import com.jinsung.safety_road_inclass.domain.workstop.entity.ReportStatus;
import com.jinsung.safety_road_inclass.domain.workstop.service.WorkStopService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * WorkStopController - 작업중지권 REST API
 *
 * 중대재해처벌법 증거자료 API:
 * - 모든 신고·상태변경·보호기간이 DB에 불변 기록됨
 * - 법적 분쟁 시 타임라인 증거로 활용 가능
 */
@Tag(name = "WorkStop", description = "작업중지권 관리 API (중대재해처벌법 증거자료)")
@RestController
@RequestMapping("/api/v1/work-stop-reports")
@RequiredArgsConstructor
@Slf4j
public class WorkStopController {

    private final WorkStopService workStopService;

    @Operation(summary = "작업중지 신고 등록", description = "근로자가 위험 상황을 신고하고 작업을 중지합니다. 30일 보호 기간이 자동 시작됩니다.")
    @PostMapping
    public ApiResponse<WorkStopReportResponse> createReport(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody WorkStopReportRequest request) {
        return ApiResponse.success(workStopService.createReport(request, currentUser));
    }

    @Operation(summary = "작업중지 신고 목록 조회", description = "상태별 필터 가능. 미전달 시 전체 조회.")
    @GetMapping
    public ApiResponse<List<WorkStopReportResponse>> getReports(
            @RequestParam(required = false) ReportStatus status) {
        return ApiResponse.success(workStopService.getReports(status));
    }

    @Operation(summary = "작업중지 신고 상세 조회", description = "상태 변경 이력(타임라인)을 포함한 상세 정보를 반환합니다.")
    @GetMapping("/{reportId}")
    public ApiResponse<WorkStopReportResponse> getReportById(
            @PathVariable Long reportId) {
        return ApiResponse.success(workStopService.getReportById(reportId));
    }

    @Operation(summary = "작업중지 신고 상태 변경", description = "상태 변경 시 이력이 자동 기록됩니다. (접수확인→조사중→해결→작업재개)")
    @PutMapping("/{reportId}/status")
    public ApiResponse<WorkStopReportResponse> updateStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
            @PathVariable Long reportId,
            @Valid @RequestBody WorkStopStatusUpdateRequest request) {
        return ApiResponse.success(workStopService.updateStatus(reportId, request, currentUser));
    }

    @Operation(summary = "작업중지 통계 조회", description = "최근 30일 기준 통계 (총 신고, 해결율, 평균 대응시간 등)")
    @GetMapping("/stats")
    public ApiResponse<WorkStopStatsResponse> getStats() {
        return ApiResponse.success(workStopService.getStats());
    }

    @Operation(summary = "보호 기간 조회", description = "작업중지권 행사자의 30일 보호 기간 목록을 조회합니다.")
    @GetMapping("/protections")
    public ApiResponse<List<WorkStopProtectionResponse>> getProtections(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(workStopService.getProtections(currentUser));
    }

    @Operation(summary = "보복 행위 신고", description = "작업중지권 행사 후 불이익 처우(보복) 발생 시 신고합니다.")
    @PostMapping("/retaliation")
    public ApiResponse<Void> createRetaliationReport(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody RetaliationReportRequest request) {
        workStopService.createRetaliationReport(request, currentUser);
        return ApiResponse.success();
    }
}

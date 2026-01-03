package com.jinsung.safety_road_inclass.domain.risk.controller;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.risk.dto.*;
import com.jinsung.safety_road_inclass.domain.risk.entity.RiskLevel;
import com.jinsung.safety_road_inclass.domain.risk.service.RiskService;
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
 * RiskController - 위험성 평가 REST API
 */
@Tag(name = "Risk", description = "위험성 평가 관리 API")
@RestController
@RequestMapping("/api/v1/risks")
@RequiredArgsConstructor
@Slf4j
public class RiskController {

    private final RiskService riskService;

    @Operation(summary = "평가 대상 위험 항목 조회", description = "평가가 필요한 위험 항목 목록을 조회합니다.")
    @GetMapping("/pending")
    public ApiResponse<List<PendingRiskResponse>> getPendingRisks() {
        List<PendingRiskResponse> risks = riskService.getPendingRisks();
        return ApiResponse.success(risks);
    }

    @Operation(summary = "위험성 평가 등록", description = "체크리스트 항목에 대한 위험성 평가를 등록합니다.")
    @PostMapping("/{checklistItemId}/assess")
    public ApiResponse<RiskAssessmentResponse> assessRisk(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
            @PathVariable Long checklistItemId,
            @Valid @RequestBody RiskAssessRequest request) {
        
        log.info("위험성 평가 요청: itemId={}, frequency={}, severity={}", 
                 checklistItemId, request.getFrequency(), request.getSeverity());
        
        RiskAssessmentResponse response = riskService.assess(checklistItemId, request, currentUser);
        
        return ApiResponse.success(response);
    }

    @Operation(summary = "위험성 평가 상세 조회", description = "평가 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{assessmentId}")
    public ApiResponse<RiskAssessmentResponse> getAssessmentDetail(
            @PathVariable Long assessmentId) {
        
        RiskAssessmentResponse response = riskService.getAssessmentDetail(assessmentId);
        
        return ApiResponse.success(response);
    }

    @Operation(summary = "고위험 항목 조회", description = "HIGH, CRITICAL 레벨의 위험 항목을 조회합니다.")
    @GetMapping("/high-risk")
    public ApiResponse<List<RiskAssessmentResponse>> getHighRiskItems() {
        List<RiskAssessmentResponse> risks = riskService.getHighRiskItems();
        return ApiResponse.success(risks);
    }

    @Operation(summary = "위험 레벨별 조회", description = "특정 위험 레벨의 항목을 조회합니다.")
    @GetMapping("/level/{level}")
    public ApiResponse<List<RiskAssessmentResponse>> getAssessmentsByLevel(
            @PathVariable RiskLevel level) {
        
        List<RiskAssessmentResponse> risks = riskService.getAssessmentsByLevel(level);
        
        return ApiResponse.success(risks);
    }

    @Operation(summary = "미완료 대책 목록 조회", description = "완료되지 않은 개선 대책 목록을 조회합니다.")
    @GetMapping("/countermeasures/incomplete")
    public ApiResponse<List<CountermeasureResponse>> getIncompleteCountermeasures() {
        List<CountermeasureResponse> countermeasures = riskService.getIncompleteCountermeasures();
        return ApiResponse.success(countermeasures);
    }

    @Operation(summary = "기한 초과 대책 조회", description = "완료 예정일이 지난 대책을 조회합니다.")
    @GetMapping("/countermeasures/overdue")
    public ApiResponse<List<CountermeasureResponse>> getOverdueCountermeasures() {
        List<CountermeasureResponse> countermeasures = riskService.getOverdueCountermeasures();
        return ApiResponse.success(countermeasures);
    }

    @Operation(summary = "대책 완료 처리", description = "개선 대책을 완료 상태로 변경합니다.")
    @PatchMapping("/countermeasures/{countermeasureId}/complete")
    public ApiResponse<CountermeasureResponse> completeCountermeasure(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
            @PathVariable Long countermeasureId) {
        
        log.info("대책 완료 처리 요청: countermeasureId={}, userId={}", countermeasureId, currentUser.getId());
        
        CountermeasureResponse response = riskService.completeCountermeasure(countermeasureId, currentUser);
        
        return ApiResponse.success(response);
    }
}


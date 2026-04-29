package com.jinsung.safety_road_inclass.domain.alert.controller;

import com.jinsung.safety_road_inclass.domain.alert.dto.AlertRequest;
import com.jinsung.safety_road_inclass.domain.alert.dto.AlertResponse;
import com.jinsung.safety_road_inclass.domain.alert.service.AlertService;
import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
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
 * AlertController - 알림 REST API
 */
@Tag(name = "Alert", description = "시스템 알림 관리 API")
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
@Slf4j
public class AlertController {

    private final AlertService alertService;

    @Operation(summary = "모든 알림 조회", description = "모든 알림을 우선순위 높은 순, 최신순으로 조회합니다.")
    @GetMapping
    public ApiResponse<List<AlertResponse>> getAllAlerts() {
        List<AlertResponse> alerts = alertService.getAllAlerts();
        return ApiResponse.success(alerts);
    }

    @Operation(summary = "활성 알림 조회", description = "현재 시간 기준 활성화된 알림만 조회합니다.")
    @GetMapping("/active")
    public ApiResponse<List<AlertResponse>> getActiveAlerts(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {
        Long userId = principal != null ? principal.userId() : null;
        List<AlertResponse> alerts = alertService.getActiveAlerts(userId);
        return ApiResponse.success(alerts);
    }

    @Operation(summary = "알림 상세 조회", description = "특정 알림의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ApiResponse<AlertResponse> getAlert(@PathVariable Long id) {
        AlertResponse alert = alertService.getAlert(id);
        return ApiResponse.success(alert);
    }

    @Operation(summary = "알림 생성", description = "새 알림을 생성합니다. (SUPERVISOR 또는 SAFETY_MANAGER 권한 필요)")
    @PostMapping
    public ApiResponse<AlertResponse> createAlert(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody AlertRequest request) {

        Long userId = principal != null ? principal.userId() : null;
        log.info("알림 생성 요청: title={}, userId={}", request.getTitle(), userId);
        AlertResponse alert = alertService.createAlert(request, userId);
        return ApiResponse.success(alert);
    }

    @Operation(summary = "알림 수정", description = "알림을 수정합니다. (SAFETY_MANAGER 권한 필요)")
    @PutMapping("/{id}")
    public ApiResponse<AlertResponse> updateAlert(
            @PathVariable Long id,
            @Valid @RequestBody AlertRequest request) {
        
        log.info("알림 수정 요청: id={}", id);
        AlertResponse alert = alertService.updateAlert(id, request);
        return ApiResponse.success(alert);
    }

    @Operation(summary = "알림 삭제", description = "알림을 삭제합니다. (SAFETY_MANAGER 권한 필요)")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAlert(@PathVariable Long id) {
        log.info("알림 삭제 요청: id={}", id);
        alertService.deleteAlert(id);
        return ApiResponse.success();
    }
}

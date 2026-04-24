package com.jinsung.safety_road_inclass.domain.hazardcycle.controller;

import com.jinsung.safety_road_inclass.domain.hazardcycle.dto.HazardVerifyResponse;
import com.jinsung.safety_road_inclass.domain.hazardcycle.service.HazardCycleService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hazard Cycle 공개 검증 엔드포인트 — 위험성평가표 PDF의 QR을 스캔했을 때 호출.
 * 인증 없이 호출 가능 (SecurityConfig에서 permit).
 */
@Tag(name = "Hazard Verify", description = "Hazard Cycle 원본 검증 API (QR 공개)")
@RestController
@RequestMapping("/api/v1/hazard-verify")
@RequiredArgsConstructor
public class HazardVerifyController {

    private final HazardCycleService hazardCycleService;

    @Operation(summary = "인증번호 기반 원본 검증",
            description = "위험성평가표 PDF의 QR 코드에서 추출한 cert number로 원본 사이클의 해시/상태를 확인합니다.")
    @GetMapping("/{certNumber}")
    public ApiResponse<HazardVerifyResponse> verify(@PathVariable String certNumber) {
        return ApiResponse.success(hazardCycleService.verifyByCertNumber(certNumber));
    }
}

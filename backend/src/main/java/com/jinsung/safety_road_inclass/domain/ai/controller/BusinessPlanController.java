package com.jinsung.safety_road_inclass.domain.ai.controller;

import com.jinsung.safety_road_inclass.domain.ai.dto.BusinessPlanRequest;
import com.jinsung.safety_road_inclass.domain.ai.dto.BusinessPlanResponse;
import com.jinsung.safety_road_inclass.domain.ai.service.BusinessPlanService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * GEMS AI 분석 Controller
 * - 프론트엔드 가이드 형식에 맞춤
 * - POST /api/v1/business-plan/generate
 */
@Tag(name = "Business Plan (GEMS AI)", description = "GEMS AI 기반 위험 분석 API")
@RestController
@RequestMapping("/api/v1/business-plan")
@RequiredArgsConstructor
@Slf4j
public class BusinessPlanController {

    private final BusinessPlanService businessPlanService;

    @Operation(
        summary = "위험 분석 요청", 
        description = "텍스트 또는 사진을 기반으로 위험 요인을 분석하고 조치 방안을 제안합니다. (Gemini API 연동)"
    )
    @PostMapping("/generate")
    public ApiResponse<BusinessPlanResponse> generate(@RequestBody BusinessPlanRequest request) {
        log.info("GEMS AI 분석 요청 수신: inputType={}", request.getInputType());
        
        BusinessPlanResponse response = businessPlanService.generate(request);
        
        return ApiResponse.success(response);
    }

    @Operation(
        summary = "분석 기록 조회", 
        description = "사용자의 AI 분석 기록을 조회합니다. (현재 Mock 데이터 반환)"
    )
    @GetMapping("/history")
    public ApiResponse<List<Map<String, Object>>> getHistory() {
        // Mock 기록 데이터
        List<Map<String, Object>> history = List.of(
            Map.of(
                "analysisId", "analysis-2025-12-17-abc12345",
                "riskFactor", "고소 작업 중 안전대 미체결",
                "riskLevel", "HIGH",
                "referenceCode", "KOSHA-G-2023-01",
                "analyzedAt", LocalDateTime.now().minusHours(2)
            ),
            Map.of(
                "analysisId", "analysis-2025-12-17-def67890",
                "riskFactor", "가연성 물질 주변 화기 작업",
                "riskLevel", "CRITICAL",
                "referenceCode", "KOSHA-M-2023-05",
                "analyzedAt", LocalDateTime.now().minusDays(1)
            )
        );
        
        return ApiResponse.success(history);
    }

    @Operation(
        summary = "서비스 상태 확인", 
        description = "GEMS AI 서비스가 정상 동작하는지 확인합니다."
    )
    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        return ApiResponse.success("GEMS AI Service is running (Gemini API Mode)");
    }
}


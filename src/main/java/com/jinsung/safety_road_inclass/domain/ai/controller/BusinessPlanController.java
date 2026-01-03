package com.jinsung.safety_road_inclass.domain.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinsung.safety_road_inclass.domain.ai.dto.BusinessPlanRequest;
import com.jinsung.safety_road_inclass.domain.ai.dto.BusinessPlanResponse;
import com.jinsung.safety_road_inclass.domain.ai.service.BusinessPlanService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * GEMS AI 분석 Controller
 * - 프론트엔드 가이드 형식에 맞춤
 * - POST /api/v1/business-plan/generate
 */
@Tag(name = "GEMS AI 분석", description = "안전 지능 시스템 위험 분석 API")
@RestController
@RequestMapping("/api/v1/business-plan")
@RequiredArgsConstructor
@Slf4j
public class BusinessPlanController {

    private final BusinessPlanService businessPlanService;
    private final ObjectMapper objectMapper;

    @Operation(
        summary = "위험 상황 분석",
        description = "AI가 위험 요인을 분석하고 조치 방안을 제시합니다. 텍스트 또는 사진을 기반으로 분석합니다.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "위험 상황 분석 요청",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BusinessPlanRequest.class),
                examples = @ExampleObject(
                    name = "텍스트 분석 요청 예시",
                    value = """
                        {
                          "inputType": "TEXT",
                          "inputText": "건설 현장 2층 비계 작업 중 안전난간이 심하게 흔들리고 있습니다. 작업자 3명이 해당 구역에서 철골 용접 작업을 진행 중이며, 안전대 체결 상태가 불량하여 추락 사고 위험이 매우 높은 상황입니다.",
                          "photoId": null,
                          "context": {
                            "workType": "construction",
                            "location": "2층 비계",
                            "workerCount": 3,
                            "currentTask": "철골 용접 작업"
                          }
                        }
                        """
                )
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "분석 성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ApiResponse.class),
            examples = @ExampleObject(
                name = "성공 응답 예시",
                value = """
                    {
                      "success": true,
                      "data": {
                        "riskFactor": "고소 작업 중 안전대 미체결",
                        "remediationSteps": [
                          "즉시 작업을 중단하고 안전한 장소로 이동하십시오.",
                          "안전대 및 부속품의 상태를 점검하십시오.",
                          "안전대 체결 후 2인 1조로 작업을 재개하십시오."
                        ],
                        "referenceCode": "KOSHA-G-2023-01",
                        "actionRecordId": "550e8400-e29b-41d4-a716-446655440000",
                        "riskLevel": "HIGH",
                        "analysisId": "analysis-2024-12-17-001",
                        "analyzedAt": "2024-12-17T10:30:00.000Z"
                      },
                      "error": null
                    }
                    """
            )
        )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 (입력 값 검증 실패)",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "에러 응답 예시",
                value = """
                    {
                      "success": false,
                      "data": null,
                      "error": {
                        "code": "INVALID_INPUT",
                        "message": "입력 값 검증에 실패했습니다.",
                        "details": {
                          "inputText": "필수 항목입니다."
                        }
                      }
                    }
                    """
            )
        )
    )
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<BusinessPlanResponse>> generate(
            @RequestBody @Valid BusinessPlanRequest request) {
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        
        log.info("[GEMS AI 요청 시작] requestId={}, inputType={}, inputTextLength={}", 
                 requestId, 
                 request.getInputType(),
                 request.getInputText() != null ? request.getInputText().length() : 0);
        
        if (log.isDebugEnabled()) {
            try {
                log.debug("[GEMS AI 요청 상세] requestId={}, request={}", requestId, 
                         objectMapper.writeValueAsString(request));
            } catch (Exception e) {
                log.debug("[GEMS AI 요청 상세] requestId={}, request 파싱 실패: {}", requestId, e.getMessage());
            }
        }
        
        try {
            BusinessPlanResponse response = businessPlanService.generate(request, requestId);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("[GEMS AI 요청 성공] requestId={}, analysisId={}, riskLevel={}, duration={}ms", 
                     requestId, 
                     response.getAnalysisId(),
                     response.getRiskLevel(),
                     duration);
            
            if (log.isDebugEnabled()) {
                try {
                    log.debug("[GEMS AI 응답 상세] requestId={}, response={}", requestId,
                             objectMapper.writeValueAsString(response));
                } catch (Exception e) {
                    log.debug("[GEMS AI 응답 상세] requestId={}, response 파싱 실패: {}", requestId, e.getMessage());
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[GEMS AI 요청 실패] requestId={}, duration={}ms, error={}", 
                      requestId, duration, e.getMessage(), e);
            throw e;
        }
    }

    @Operation(
        summary = "분석 기록 조회", 
        description = "사용자의 AI 분석 기록을 조회합니다. (현재 Mock 데이터 반환)"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "조회 성공"
    )
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getHistory() {
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
        
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    @Operation(
        summary = "서비스 상태 확인", 
        description = "GEMS AI 서비스가 정상 동작하는지 확인합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "서비스 정상"
    )
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
            ApiResponse.success("GEMS AI Service is running (Gemini API Mode)")
        );
    }
}


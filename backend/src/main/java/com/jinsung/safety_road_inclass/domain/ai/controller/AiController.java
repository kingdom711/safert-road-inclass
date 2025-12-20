package com.jinsung.safety_road_inclass.domain.ai.controller;

import com.jinsung.safety_road_inclass.domain.ai.dto.AiAnalysisRequest;
import com.jinsung.safety_road_inclass.domain.ai.dto.AiAnalysisResponse;
import com.jinsung.safety_road_inclass.domain.ai.dto.AiPhotoAnalysisResponse;
import com.jinsung.safety_road_inclass.domain.ai.service.AiAnalysisService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * AI 분석 Controller
 * - 텍스트 기반 위험 분석
 * - 이미지 기반 위험 분석
 */
@Tag(name = "AI Analysis", description = "AI 기반 위험 분석 API (Gemini API 연동)")
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiAnalysisService aiAnalysisService;

    @Operation(
        summary = "텍스트 기반 위험 분석", 
        description = "체크리스트 내용을 Gemini AI가 분석하여 위험 요인과 개선 대책을 제안합니다."
    )
    @PostMapping("/analyze")
    public ApiResponse<AiAnalysisResponse> analyzeText(@Valid @RequestBody AiAnalysisRequest request) {
        log.info("AI 텍스트 분석 요청 수신: checklistId={}", request.getChecklistId());
        
        AiAnalysisResponse response = aiAnalysisService.analyzeText(request);
        
        return ApiResponse.success(response);
    }

    @Operation(
        summary = "이미지 기반 위험 분석", 
        description = "업로드된 작업 사진을 AI가 분석하여 위험 요소를 감지합니다. (현재 Mock 데이터 반환)"
    )
    @PostMapping(value = "/analyze-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AiPhotoAnalysisResponse> analyzePhoto(
            @RequestParam("photo") MultipartFile photo) throws IOException {
        
        log.info("AI 이미지 분석 요청 수신: filename={}, size={}", 
                 photo.getOriginalFilename(), photo.getSize());
        
        AiPhotoAnalysisResponse response = aiAnalysisService.analyzePhoto(
                photo.getBytes(), 
                photo.getOriginalFilename()
        );
        
        return ApiResponse.success(response);
    }

    @Operation(
        summary = "AI 서비스 상태 확인", 
        description = "AI 분석 서비스가 정상 동작하는지 확인합니다."
    )
    @GetMapping("/health")
    public ApiResponse<String> healthCheck() {
        return ApiResponse.success("AI Analysis Service is running (Gemini API Mode)");
    }
}



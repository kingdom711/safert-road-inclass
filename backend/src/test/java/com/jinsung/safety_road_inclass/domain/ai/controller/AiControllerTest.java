package com.jinsung.safety_road_inclass.domain.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinsung.safety_road_inclass.domain.ai.dto.AiAnalysisRequest;
import com.jinsung.safety_road_inclass.domain.ai.dto.AiAnalysisResponse;
import com.jinsung.safety_road_inclass.domain.ai.dto.AiPhotoAnalysisResponse;
import com.jinsung.safety_road_inclass.domain.ai.service.AiAnalysisService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AiController 통합 테스트
 */
@WebMvcTest(AiController.class)
@DisplayName("AiController 통합 테스트")
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiAnalysisService aiAnalysisService;

    @BeforeEach
    void setUp() {
        // 기본 Mock 설정
    }

    @Test
    @DisplayName("POST /api/v1/ai/analyze - 정상 요청 성공")
    void analyzeText_Success() throws Exception {
        // Given
        AiAnalysisRequest request = AiAnalysisRequest.builder()
            .checklistId(1L)
            .content("안전모를 착용하지 않고 작업을 진행했습니다.")
            .build();

        AiAnalysisResponse mockResponse = AiAnalysisResponse.builder()
            .riskLevel("N/A")
            .riskFactors(List.of("원본 문서: 안전모를 착용하지 않고 작업을 진행했습니다."))
            .recommendations(List.of("안전모를 반드시 착용하고 작업을 진행해야 합니다."))
            .analysisSource("Spring AI Gemini (문서 보강)")
            .analyzedAt(LocalDateTime.now())
            .message("✅ Spring AI Gemini를 통한 문서 보강이 완료되었습니다.")
            .usage(AiAnalysisResponse.Usage.builder()
                .promptTokens(50)
                .candidatesTokens(30)
                .totalTokens(80)
                .build())
            .build();

        when(aiAnalysisService.analyzeText(any(AiAnalysisRequest.class)))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.riskLevel").value("N/A"))
            .andExpect(jsonPath("$.data.recommendations").isArray())
            .andExpect(jsonPath("$.data.usage.promptTokens").value(50))
            .andExpect(jsonPath("$.data.usage.totalTokens").value(80));
    }

    @Test
    @DisplayName("POST /api/v1/ai/analyze - 유효성 검증 실패: checklistId null")
    void analyzeText_ValidationFailure_ChecklistIdNull() throws Exception {
        // Given
        AiAnalysisRequest request = AiAnalysisRequest.builder()
            .checklistId(null)
            .content("안전 관련 내용")
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/ai/analyze - 유효성 검증 실패: content null")
    void analyzeText_ValidationFailure_ContentNull() throws Exception {
        // Given
        AiAnalysisRequest request = AiAnalysisRequest.builder()
            .checklistId(1L)
            .content(null)
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/ai/analyze - 유효성 검증 실패: content 빈 문자열")
    void analyzeText_ValidationFailure_ContentEmpty() throws Exception {
        // Given
        AiAnalysisRequest request = AiAnalysisRequest.builder()
            .checklistId(1L)
            .content("")
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/ai/analyze - 유효성 검증 실패: content 길이 초과")
    void analyzeText_ValidationFailure_ContentTooLong() throws Exception {
        // Given
        String longContent = "안".repeat(5001); // 5001자
        AiAnalysisRequest request = AiAnalysisRequest.builder()
            .checklistId(1L)
            .content(longContent)
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/ai/analyze-photo - 정상 요청 성공")
    void analyzePhoto_Success() throws Exception {
        // Given
        MockMultipartFile photo = new MockMultipartFile(
            "photo",
            "test.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        AiPhotoAnalysisResponse mockResponse = AiPhotoAnalysisResponse.builder()
            .riskLevel("MEDIUM")
            .detectedIssues(List.of("사다리 고정 상태 불안정"))
            .tags(List.of("고소작업"))
            .analysisSource("MOCK_DATA")
            .analyzedAt(LocalDateTime.now())
            .message("✅ 백엔드 AI 이미지 분석 API 호출 성공!")
            .build();

        when(aiAnalysisService.analyzePhoto(any(byte[].class), any(String.class)))
            .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(multipart("/api/v1/ai/analyze-photo")
                .file(photo))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.riskLevel").value("MEDIUM"))
            .andExpect(jsonPath("$.data.detectedIssues").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/ai/analyze-photo - 파일 없음")
    void analyzePhoto_NoFile() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/api/v1/ai/analyze-photo"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/ai/health - 정상 응답")
    void healthCheck_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/ai/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value("AI Analysis Service is running (Gemini API Mode)"));
    }

    @Test
    @DisplayName("POST /api/v1/ai/analyze - 잘못된 Content-Type")
    void analyzeText_InvalidContentType() throws Exception {
        // Given
        AiAnalysisRequest request = AiAnalysisRequest.builder()
            .checklistId(1L)
            .content("테스트 내용")
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/ai/analyze")
                .contentType(MediaType.TEXT_PLAIN)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnsupportedMediaType());
    }
}


package com.jinsung.safety_road_inclass.domain.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinsung.safety_road_inclass.domain.ai.dto.AiAnalysisRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AiController 통합 테스트
 * - 실제 Spring AI ChatModel을 사용한 Gemini API 호출 통합 테스트
 * - GEMINI_API_KEY 환경변수가 설정되어 있을 때만 실행
 * 
 * 실행 방법:
 * 1. 환경변수 설정: export GEMINI_API_KEY=your_api_key
 * 2. IDE에서 실행 또는: ./gradlew test --tests "*AiControllerIntegrationTest"
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Tag("integration")
@Tag("gemini-api")
@DisplayName("AiController 통합 테스트 (실제 Gemini API 호출)")
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class AiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/ai/analyze - 실제 Gemini API 호출 통합 테스트")
    void analyzeText_RealApiCall_IntegrationTest() throws Exception {
        // Given
        AiAnalysisRequest request = AiAnalysisRequest.builder()
            .checklistId(1L)
            .content("안전모를 착용하지 않고 작업을 진행했습니다. 이는 매우 위험한 행동입니다.")
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").exists())
            .andExpect(jsonPath("$.data.riskLevel").exists())
            .andExpect(jsonPath("$.data.recommendations").isArray())
            .andExpect(jsonPath("$.data.recommendations").isNotEmpty())
            .andExpect(jsonPath("$.data.analysisSource").value("Spring AI Gemini (문서 보강)"))
            .andExpect(jsonPath("$.data.usage").exists())
            .andExpect(jsonPath("$.data.usage.promptTokens").exists())
            .andExpect(jsonPath("$.data.usage.candidatesTokens").exists())
            .andExpect(jsonPath("$.data.usage.totalTokens").exists())
            .andDo(result -> {
                String response = result.getResponse().getContentAsString();
                System.out.println("=== 실제 Gemini API 호출 통합 테스트 결과 ===");
                System.out.println(response);
            });
    }

    @Test
    @DisplayName("POST /api/v1/ai/analyze - 긴 문서 실제 API 호출")
    void analyzeText_RealApiCall_LongContent() throws Exception {
        // Given
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            longContent.append("안전 관련 내용입니다. ").append(i).append("\n");
        }

        AiAnalysisRequest request = AiAnalysisRequest.builder()
            .checklistId(1L)
            .content(longContent.toString())
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/ai/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.recommendations").isArray())
            .andExpect(jsonPath("$.data.usage.totalTokens").exists());
    }
}


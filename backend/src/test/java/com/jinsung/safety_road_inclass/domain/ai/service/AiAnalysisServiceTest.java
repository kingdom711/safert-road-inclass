package com.jinsung.safety_road_inclass.domain.ai.service;

import com.jinsung.safety_road_inclass.domain.ai.dto.AiAnalysisRequest;
import com.jinsung.safety_road_inclass.domain.ai.dto.AiAnalysisResponse;
import com.jinsung.safety_road_inclass.domain.ai.service.DocumentEnhancementService.DocumentEnhancementResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * AiAnalysisService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AiAnalysisService 단위 테스트")
class AiAnalysisServiceTest {

    @Mock
    private DocumentEnhancementService documentEnhancementService;

    @InjectMocks
    private AiAnalysisService aiAnalysisService;

    @BeforeEach
    void setUp() {
        // 기본 Mock 설정
    }

    @Test
    @DisplayName("정상적인 문서 분석 요청 - 성공")
    void analyzeText_Success() {
        // Given
        AiAnalysisRequest request = AiAnalysisRequest.builder()
            .checklistId(1L)
            .content("안전모를 착용하지 않고 작업을 진행했습니다.")
            .build();

        DocumentEnhancementResult enhancementResult = DocumentEnhancementResult.builder()
            .enhancedContent("안전모를 반드시 착용하고 작업을 진행해야 합니다.")
            .promptTokens(50)
            .generationTokens(30)
            .totalTokens(80)
            .build();

        when(documentEnhancementService.enhanceDocument(anyString()))
            .thenReturn(enhancementResult);

        // When
        AiAnalysisResponse response = aiAnalysisService.analyzeText(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRiskLevel()).isEqualTo("N/A");
        assertThat(response.getRiskFactors()).isNotEmpty();
        assertThat(response.getRecommendations()).isNotEmpty();
        assertThat(response.getAnalysisSource()).isEqualTo("Spring AI Gemini (문서 보강)");
        assertThat(response.getAnalyzedAt()).isNotNull();
        assertThat(response.getMessage()).contains("문서 보강이 완료되었습니다");
        assertThat(response.getUsage()).isNotNull();
        assertThat(response.getUsage().getPromptTokens()).isEqualTo(50);
        assertThat(response.getUsage().getCandidatesTokens()).isEqualTo(30);
        assertThat(response.getUsage().getTotalTokens()).isEqualTo(80);
    }

    @Test
    @DisplayName("긴 문서 내용 분석 - recommendations 10줄 제한")
    void analyzeText_LongContent_RecommendationsLimited() {
        // Given
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            longContent.append("안전 관련 내용입니다.\n");
        }

        AiAnalysisRequest request = AiAnalysisRequest.builder()
            .checklistId(1L)
            .content(longContent.toString())
            .build();

        StringBuilder enhancedContent = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            enhancedContent.append("보강된 내용 ").append(i).append("\n");
        }

        DocumentEnhancementResult enhancementResult = DocumentEnhancementResult.builder()
            .enhancedContent(enhancedContent.toString())
            .promptTokens(100)
            .generationTokens(200)
            .totalTokens(300)
            .build();

        when(documentEnhancementService.enhanceDocument(anyString()))
            .thenReturn(enhancementResult);

        // When
        AiAnalysisResponse response = aiAnalysisService.analyzeText(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRecommendations()).isNotNull();
        assertThat(response.getRecommendations().size()).isLessThanOrEqualTo(10);
    }

    @Test
    @DisplayName("null content 처리 - Fallback")
    void analyzeText_NullContent_Fallback() {
        // Given
        AiAnalysisRequest request = AiAnalysisRequest.builder()
            .checklistId(1L)
            .content(null)
            .build();

        DocumentEnhancementResult enhancementResult = DocumentEnhancementResult.builder()
            .enhancedContent(null)
            .promptTokens(0)
            .generationTokens(0)
            .totalTokens(0)
            .build();

        when(documentEnhancementService.enhanceDocument(anyString()))
            .thenReturn(enhancementResult);

        // When
        AiAnalysisResponse response = aiAnalysisService.analyzeText(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRiskFactors()).isEmpty();
        assertThat(response.getRecommendations()).isEmpty();
    }

    @Test
    @DisplayName("빈 문자열 content 처리")
    void analyzeText_EmptyContent() {
        // Given
        AiAnalysisRequest request = AiAnalysisRequest.builder()
            .checklistId(1L)
            .content("")
            .build();

        DocumentEnhancementResult enhancementResult = DocumentEnhancementResult.builder()
            .enhancedContent("")
            .promptTokens(0)
            .generationTokens(0)
            .totalTokens(0)
            .build();

        when(documentEnhancementService.enhanceDocument(anyString()))
            .thenReturn(enhancementResult);

        // When
        AiAnalysisResponse response = aiAnalysisService.analyzeText(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRiskFactors()).isEmpty();
        assertThat(response.getRecommendations()).isEmpty();
    }

    @Test
    @DisplayName("100자 이상 content - riskFactors 요약 처리")
    void analyzeText_LongContent_RiskFactorsSummary() {
        // Given
        String longContent = "안".repeat(150); // 150자
        AiAnalysisRequest request = AiAnalysisRequest.builder()
            .checklistId(1L)
            .content(longContent)
            .build();

        DocumentEnhancementResult enhancementResult = DocumentEnhancementResult.builder()
            .enhancedContent("보강된 내용")
            .promptTokens(50)
            .generationTokens(30)
            .totalTokens(80)
            .build();

        when(documentEnhancementService.enhanceDocument(anyString()))
            .thenReturn(enhancementResult);

        // When
        AiAnalysisResponse response = aiAnalysisService.analyzeText(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRiskFactors()).isNotEmpty();
        assertThat(response.getRiskFactors().get(0)).contains("...");
    }

    @Test
    @DisplayName("토큰 사용량 정보 없음 - usage null")
    void analyzeText_NoTokenUsage_UsageNull() {
        // Given
        AiAnalysisRequest request = AiAnalysisRequest.builder()
            .checklistId(1L)
            .content("테스트 내용")
            .build();

        DocumentEnhancementResult enhancementResult = DocumentEnhancementResult.builder()
            .enhancedContent("보강된 내용")
            .promptTokens(null)
            .generationTokens(null)
            .totalTokens(null)
            .build();

        when(documentEnhancementService.enhanceDocument(anyString()))
            .thenReturn(enhancementResult);

        // When
        AiAnalysisResponse response = aiAnalysisService.analyzeText(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUsage()).isNull();
    }

    @Test
    @DisplayName("여러 줄 recommendations 처리")
    void analyzeText_MultiLineRecommendations() {
        // Given
        AiAnalysisRequest request = AiAnalysisRequest.builder()
            .checklistId(1L)
            .content("안전 관련 내용")
            .build();

        String multiLineContent = """
            첫 번째 권장사항
            두 번째 권장사항
            세 번째 권장사항
            """;

        DocumentEnhancementResult enhancementResult = DocumentEnhancementResult.builder()
            .enhancedContent(multiLineContent)
            .promptTokens(50)
            .generationTokens(30)
            .totalTokens(80)
            .build();

        when(documentEnhancementService.enhanceDocument(anyString()))
            .thenReturn(enhancementResult);

        // When
        AiAnalysisResponse response = aiAnalysisService.analyzeText(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRecommendations()).isNotEmpty();
        assertThat(response.getRecommendations().size()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("빈 줄 제거 처리")
    void analyzeText_EmptyLinesRemoved() {
        // Given
        AiAnalysisRequest request = AiAnalysisRequest.builder()
            .checklistId(1L)
            .content("안전 관련 내용")
            .build();

        String contentWithEmptyLines = """
            첫 번째
            
            두 번째
            
            
            세 번째
            """;

        DocumentEnhancementResult enhancementResult = DocumentEnhancementResult.builder()
            .enhancedContent(contentWithEmptyLines)
            .promptTokens(50)
            .generationTokens(30)
            .totalTokens(80)
            .build();

        when(documentEnhancementService.enhanceDocument(anyString()))
            .thenReturn(enhancementResult);

        // When
        AiAnalysisResponse response = aiAnalysisService.analyzeText(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRecommendations()).isNotEmpty();
        // 빈 줄은 제거되어야 함
        assertThat(response.getRecommendations()).doesNotContain("");
    }
}


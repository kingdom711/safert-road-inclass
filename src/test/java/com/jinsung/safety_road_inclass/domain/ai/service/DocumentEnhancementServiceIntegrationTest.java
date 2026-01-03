package com.jinsung.safety_road_inclass.domain.ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DocumentEnhancementService 통합 테스트
 * - 실제 Spring AI ChatModel을 사용한 Gemini API 호출 테스트
 * - GEMINI_API_KEY 환경변수가 설정되어 있을 때만 실행
 * 
 * 실행 방법:
 * 1. 환경변수 설정: export GEMINI_API_KEY=your_api_key
 * 2. IDE에서 실행 또는: ./gradlew test --tests "*DocumentEnhancementServiceIntegrationTest"
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Tag("integration")
@Tag("gemini-api")
@DisplayName("DocumentEnhancementService 통합 테스트 (실제 Gemini API 호출)")
@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
class DocumentEnhancementServiceIntegrationTest {

    @Autowired(required = false)
    private DocumentEnhancementService documentEnhancementService;

    @BeforeEach
    void setUp() {
        assertThat(documentEnhancementService).isNotNull();
    }

    @Test
    @DisplayName("실제 Gemini API 호출 - 정상적인 문서 보강")
    void enhanceDocument_RealApiCall_Success() {
        // Given
        String draftContent = "안전모를 착용하지 않고 작업을 진행했습니다. 이는 매우 위험한 행동입니다.";

        // When
        DocumentEnhancementService.DocumentEnhancementResult result = 
            documentEnhancementService.enhanceDocument(draftContent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEnhancedContent()).isNotNull();
        assertThat(result.getEnhancedContent()).isNotEmpty();
        
        // 실제 API 호출이므로 원본과 다른 내용이 반환되어야 함
        assertThat(result.getEnhancedContent()).isNotEqualTo(draftContent);
        assertThat(result.getEnhancedContent().length()).isGreaterThan(draftContent.length());
        
        // 토큰 사용량 정보가 있어야 함
        assertThat(result.getPromptTokens()).isNotNull();
        assertThat(result.getGenerationTokens()).isNotNull();
        assertThat(result.getTotalTokens()).isNotNull();
        assertThat(result.getTotalTokens()).isGreaterThan(0);
        
        System.out.println("=== Gemini API 호출 결과 ===");
        System.out.println("원본: " + draftContent);
        System.out.println("보강: " + result.getEnhancedContent());
        System.out.println("토큰 사용량 - Input: " + result.getPromptTokens() + 
                         ", Output: " + result.getGenerationTokens() + 
                         ", Total: " + result.getTotalTokens());
    }

    @Test
    @DisplayName("실제 Gemini API 호출 - 긴 문서 보강")
    void enhanceDocument_RealApiCall_LongContent() {
        // Given
        StringBuilder longContent = new StringBuilder();
        longContent.append("안전 관련 체크리스트입니다.\n");
        longContent.append("1. 안전모 착용 여부를 확인하세요.\n");
        longContent.append("2. 안전대 체결 상태를 점검하세요.\n");
        longContent.append("3. 작업 구역의 안전난간 상태를 확인하세요.\n");
        longContent.append("4. 작업 도구의 안전성을 검사하세요.\n");
        longContent.append("5. 응급 처치 키트의 위치를 확인하세요.");
        String draftContent = longContent.toString();

        // When
        DocumentEnhancementService.DocumentEnhancementResult result = 
            documentEnhancementService.enhanceDocument(draftContent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEnhancedContent()).isNotNull();
        assertThat(result.getEnhancedContent()).isNotEmpty();
        assertThat(result.getPromptTokens()).isNotNull();
        assertThat(result.getGenerationTokens()).isNotNull();
        assertThat(result.getTotalTokens()).isNotNull();
    }

    @Test
    @DisplayName("실제 Gemini API 호출 - 전문 용어 포함 문서")
    void enhanceDocument_RealApiCall_TechnicalTerms() {
        // Given
        String draftContent = """
            고소작업 시 안전대를 착용해야 합니다.
            작업 높이는 2층 이상이며, 비계 상태를 확인해야 합니다.
            안전난간이 제대로 설치되어 있는지 점검하세요.
            """;

        // When
        DocumentEnhancementService.DocumentEnhancementResult result = 
            documentEnhancementService.enhanceDocument(draftContent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEnhancedContent()).isNotNull();
        assertThat(result.getEnhancedContent()).isNotEmpty();
        
        // 전문 용어가 포함되어 있어야 함
        assertThat(result.getEnhancedContent()).containsAnyOf("안전대", "고소작업", "비계", "안전난간");
    }

    @Test
    @DisplayName("실제 Gemini API 호출 - null 입력 처리")
    void enhanceDocument_RealApiCall_NullInput() {
        // Given
        String draftContent = null;

        // When
        DocumentEnhancementService.DocumentEnhancementResult result = 
            documentEnhancementService.enhanceDocument(draftContent);

        // Then
        assertThat(result).isNotNull();
        // null 입력 시 Fallback 동작 또는 에러 처리 확인
    }

    @Test
    @DisplayName("실제 Gemini API 호출 - 빈 문자열 처리")
    void enhanceDocument_RealApiCall_EmptyString() {
        // Given
        String draftContent = "";

        // When
        DocumentEnhancementService.DocumentEnhancementResult result = 
            documentEnhancementService.enhanceDocument(draftContent);

        // Then
        assertThat(result).isNotNull();
        // 빈 문자열 처리 확인
    }

    @Test
    @DisplayName("실제 Gemini API 호출 - 토큰 사용량 측정 검증")
    void enhanceDocument_RealApiCall_TokenUsage() {
        // Given
        String draftContent = "안전 관련 문서입니다. 이 문서는 보강이 필요합니다.";

        // When
        DocumentEnhancementService.DocumentEnhancementResult result = 
            documentEnhancementService.enhanceDocument(draftContent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPromptTokens()).isNotNull();
        assertThat(result.getGenerationTokens()).isNotNull();
        assertThat(result.getTotalTokens()).isNotNull();
        
        // 총 토큰은 프롬프트 토큰과 생성 토큰의 합과 같거나 근사해야 함
        assertThat(result.getTotalTokens()).isGreaterThanOrEqualTo(
            (result.getPromptTokens() != null ? result.getPromptTokens() : 0) +
            (result.getGenerationTokens() != null ? result.getGenerationTokens() : 0)
        );
        
        System.out.println("=== 토큰 사용량 검증 ===");
        System.out.println("Prompt Tokens: " + result.getPromptTokens());
        System.out.println("Generation Tokens: " + result.getGenerationTokens());
        System.out.println("Total Tokens: " + result.getTotalTokens());
    }
}


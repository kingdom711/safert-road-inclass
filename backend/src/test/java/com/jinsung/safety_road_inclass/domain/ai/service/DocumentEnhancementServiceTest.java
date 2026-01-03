package com.jinsung.safety_road_inclass.domain.ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * DocumentEnhancementService 단위 테스트
 * 
 * 현재 Spring AI 의존성이 비활성화되어 Mock 구현으로 동작하므로,
 * Mock 구현 동작을 검증하고, 향후 Spring AI 활성화 시를 대비한 테스트 구조를 준비합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentEnhancementService 단위 테스트")
class DocumentEnhancementServiceTest {

    @InjectMocks
    private DocumentEnhancementService documentEnhancementService;

    @BeforeEach
    void setUp() {
        // 현재는 Mock 구현이므로 별도 설정 불필요
    }

    @Test
    @DisplayName("정상적인 문서 보강 요청 - 성공")
    void enhanceDocument_Success() {
        // Given
        String draftContent = "안전모를 착용하지 않고 작업을 진행했습니다.";

        // When
        DocumentEnhancementService.DocumentEnhancementResult result = 
            documentEnhancementService.enhanceDocument(draftContent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEnhancedContent()).isNotNull();
        assertThat(result.getEnhancedContent()).contains(draftContent);
        // Mock 구현에서는 "[임시] " 접두어가 추가됨
        assertThat(result.getEnhancedContent()).startsWith("[임시] ");
        
        // Mock 구현에서는 토큰이 0으로 반환됨
        assertThat(result.getPromptTokens()).isEqualTo(0);
        assertThat(result.getGenerationTokens()).isEqualTo(0);
        assertThat(result.getTotalTokens()).isEqualTo(0);
    }

    @Test
    @DisplayName("긴 문서 내용 처리 - 성공")
    void enhanceDocument_LongContent_Success() {
        // Given
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longContent.append("안전 관련 내용입니다. ");
        }
        String draftContent = longContent.toString();

        // When
        DocumentEnhancementService.DocumentEnhancementResult result = 
            documentEnhancementService.enhanceDocument(draftContent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEnhancedContent()).isNotNull();
        assertThat(result.getEnhancedContent().length()).isGreaterThan(draftContent.length());
    }

    @Test
    @DisplayName("null 입력 처리 - Fallback 동작")
    void enhanceDocument_NullInput_Fallback() {
        // Given
        String draftContent = null;

        // When
        DocumentEnhancementService.DocumentEnhancementResult result = 
            documentEnhancementService.enhanceDocument(draftContent);

        // Then
        assertThat(result).isNotNull();
        // Mock 구현에서는 null이 그대로 반환될 수 있음
        // 실제 Spring AI 활성화 시에는 원본 내용을 반환해야 함
    }

    @Test
    @DisplayName("빈 문자열 처리 - 성공")
    void enhanceDocument_EmptyString_Success() {
        // Given
        String draftContent = "";

        // When
        DocumentEnhancementService.DocumentEnhancementResult result = 
            documentEnhancementService.enhanceDocument(draftContent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEnhancedContent()).isNotNull();
    }

    @Test
    @DisplayName("여러 줄 문서 처리 - 성공")
    void enhanceDocument_MultiLineContent_Success() {
        // Given
        String draftContent = """
            안전모 착용 여부: 미착용
            안전난간 상태: 불안정
            작업 높이: 2층
            작업자 수: 3명
            """;

        // When
        DocumentEnhancementService.DocumentEnhancementResult result = 
            documentEnhancementService.enhanceDocument(draftContent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEnhancedContent()).isNotNull();
        assertThat(result.getEnhancedContent()).contains("안전모");
    }

    @Test
    @DisplayName("특수 문자 포함 문서 처리 - 성공")
    void enhanceDocument_SpecialCharacters_Success() {
        // Given
        String draftContent = "안전 관련 내용입니다. <script>alert('test')</script> & 특수문자!@#$%";

        // When
        DocumentEnhancementService.DocumentEnhancementResult result = 
            documentEnhancementService.enhanceDocument(draftContent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEnhancedContent()).isNotNull();
    }

    @Test
    @DisplayName("DocumentEnhancementResult 빌더 테스트")
    void documentEnhancementResult_BuilderTest() {
        // Given & When
        DocumentEnhancementService.DocumentEnhancementResult result = 
            DocumentEnhancementService.DocumentEnhancementResult.builder()
                .enhancedContent("보강된 내용")
                .promptTokens(100)
                .generationTokens(200)
                .totalTokens(300)
                .build();

        // Then
        assertThat(result.getEnhancedContent()).isEqualTo("보강된 내용");
        assertThat(result.getPromptTokens()).isEqualTo(100);
        assertThat(result.getGenerationTokens()).isEqualTo(200);
        assertThat(result.getTotalTokens()).isEqualTo(300);
    }

    @Test
    @DisplayName("DocumentEnhancementResult null 값 처리")
    void documentEnhancementResult_NullValues() {
        // Given & When
        DocumentEnhancementService.DocumentEnhancementResult result = 
            DocumentEnhancementService.DocumentEnhancementResult.builder()
                .enhancedContent(null)
                .promptTokens(null)
                .generationTokens(null)
                .totalTokens(null)
                .build();

        // Then
        assertThat(result.getEnhancedContent()).isNull();
        assertThat(result.getPromptTokens()).isNull();
        assertThat(result.getGenerationTokens()).isNull();
        assertThat(result.getTotalTokens()).isNull();
    }
}


package com.jinsung.safety_road_inclass.domain.ai.service;

import com.jinsung.safety_road_inclass.domain.ai.config.GeminiConfig;
import com.jinsung.safety_road_inclass.domain.ai.dto.gemini.GeminiAnalysisResult;
import com.jinsung.safety_road_inclass.domain.ai.dto.gemini.GeminiRequest;
import com.jinsung.safety_road_inclass.domain.ai.dto.gemini.GeminiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GeminiService 단위 테스트")
class GeminiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GeminiConfig geminiConfig;

    private GeminiService geminiService;

    @BeforeEach
    void setUp() {
        geminiService = new GeminiService(
                restTemplate,
                geminiConfig,
                new com.fasterxml.jackson.databind.ObjectMapper()
        );
    }

    @Test
    @DisplayName("analyzeImage_정상응답_파싱성공")
    void analyzeImage_정상응답_파싱성공() {
        when(geminiConfig.getFullUrl()).thenReturn("http://test-gemini");
        when(geminiConfig.getKey()).thenReturn("abcde12345");

        GeminiResponse response = new GeminiResponse();

        GeminiResponse.Part part = new GeminiResponse.Part();
        part.setText("{\"riskFactor\":\"감전 위험\",\"riskLevel\":\"HIGH\",\"remediationSteps\":[\"전원 차단\"],\"referenceCode\":\"KOSHA-E-2023-07\"}");

        GeminiResponse.Content content = new GeminiResponse.Content();
        content.setParts(java.util.List.of(part));

        GeminiResponse.Candidate candidate = new GeminiResponse.Candidate();
        candidate.setContent(content);
        response.setCandidates(java.util.List.of(candidate));

        GeminiResponse.UsageMetadata usage = new GeminiResponse.UsageMetadata();
        usage.setPromptTokenCount(10);
        usage.setCandidatesTokenCount(20);
        usage.setTotalTokenCount(30);
        response.setUsageMetadata(usage);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(GeminiResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        GeminiAnalysisResult result = geminiService.analyzeImage("img".getBytes(), "image/jpeg");

        assertThat(result.getRiskLevel()).isEqualTo("HIGH");
        assertThat(result.getRiskFactor()).contains("감전");
        assertThat(result.getReferenceCode()).isEqualTo("KOSHA-E-2023-07");
        assertThat(result.getUsageMetadata()).isNotNull();
        assertThat(result.getUsageMetadata().getTotalTokenCount()).isEqualTo(30);
    }

    @Test
    @DisplayName("analyzeImage_API실패_Fallback반환")
    void analyzeImage_API실패_Fallback반환() {
        when(geminiConfig.getFullUrl()).thenReturn("http://test-gemini");
        when(geminiConfig.getKey()).thenReturn("abcde12345");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(GeminiResponse.class)))
                .thenThrow(new RestClientException("API Down"));

        GeminiAnalysisResult result = geminiService.analyzeImage("img".getBytes(), "image/jpeg");

        assertThat(result).isNotNull();
        assertThat(result.getRiskLevel()).isNotBlank();
        assertThat(result.getRemediationSteps()).isNotEmpty();
    }

    @Test
    @DisplayName("analyzeImage_제외대상_필터링확인")
    void analyzeImage_제외대상_필터링확인() {
        when(geminiConfig.getFullUrl()).thenReturn("http://test-gemini");
        when(geminiConfig.getKey()).thenReturn("abcde12345");

        GeminiResponse response = new GeminiResponse();

        GeminiResponse.Part part = new GeminiResponse.Part();
        part.setText("{\"riskFactor\":\"훅업 작업으로 인한 위험\",\"riskLevel\":\"HIGH\",\"remediationSteps\":[\"훅업 점검\"],\"referenceCode\":\"KOSHA-L-2023-11\"}");

        GeminiResponse.Content content = new GeminiResponse.Content();
        content.setParts(java.util.List.of(part));

        GeminiResponse.Candidate candidate = new GeminiResponse.Candidate();
        candidate.setContent(content);
        response.setCandidates(java.util.List.of(candidate));

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(GeminiResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        GeminiAnalysisResult result = geminiService.analyzeImage("img".getBytes(), "image/jpeg");

        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), entityCaptor.capture(), eq(GeminiResponse.class));

        HttpEntity captured = entityCaptor.getValue();
        GeminiRequest request = (GeminiRequest) captured.getBody();
        String prompt = request.getContents().get(0).getParts().get(0).getText();

        assertThat(prompt).contains("배관 구경에 따른 질식 위험 및 훅업(Hook-up) 작업 관련 요소는 분석 대상에서 제외합니다.");
        assertThat(result.getRiskFactor()).doesNotContain("훅업");
    }
}

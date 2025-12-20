package com.jinsung.safety_road_inclass.domain.ai.service;

import com.jinsung.safety_road_inclass.domain.ai.dto.AiAnalysisRequest;
import com.jinsung.safety_road_inclass.domain.ai.dto.AiAnalysisResponse;
import com.jinsung.safety_road_inclass.domain.ai.dto.AiPhotoAnalysisResponse;
import com.jinsung.safety_road_inclass.domain.ai.dto.gemini.GeminiAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 분석 서비스
 * - Gemini API를 통한 체크리스트 기반 위험 분석
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisService {

    private final GeminiService geminiService;

    /**
     * 텍스트 기반 위험 분석 (Gemini API 연동)
     */
    public AiAnalysisResponse analyzeText(AiAnalysisRequest request) {
        log.info("[AI 분석 요청] checklistId={}, content={}", 
                 request.getChecklistId(), 
                 request.getContent() != null ? request.getContent().substring(0, Math.min(50, request.getContent().length())) + "..." : "null");

        // Gemini API 호출
        GeminiAnalysisResult geminiResult = geminiService.analyzeRisk(
            request.getContent(),
            null, // workType
            null, // location
            null, // workerCount
            null  // currentTask
        );
        
        // Gemini 응답을 AiAnalysisResponse로 변환
        return convertToAiAnalysisResponse(geminiResult);
    }
    
    /**
     * GeminiAnalysisResult를 AiAnalysisResponse로 변환
     */
    private AiAnalysisResponse convertToAiAnalysisResponse(GeminiAnalysisResult geminiResult) {
        // riskFactor를 리스트로 변환
        List<String> riskFactors = new ArrayList<>();
        if (geminiResult.getRiskFactor() != null && !geminiResult.getRiskFactor().isEmpty()) {
            riskFactors.add(geminiResult.getRiskFactor());
        }
        
        // riskLevel 매핑 (CRITICAL, HIGH, MEDIUM, LOW → 그대로 사용)
        String riskLevel = geminiResult.getRiskLevel() != null 
            ? geminiResult.getRiskLevel() 
            : "MEDIUM";
        
        // remediationSteps → recommendations
        List<String> recommendations = geminiResult.getRemediationSteps() != null
            ? geminiResult.getRemediationSteps()
            : new ArrayList<>();
        
        // analysisSource 구성
        String analysisSource = "Gemini API";
        if (geminiResult.getReferenceCode() != null && !geminiResult.getReferenceCode().isEmpty()) {
            analysisSource += " (" + geminiResult.getReferenceCode() + ")";
        }
        
        // 토큰 사용량 정보 변환
        AiAnalysisResponse.Usage usage = null;
        if (geminiResult.getUsageMetadata() != null) {
            usage = AiAnalysisResponse.Usage.builder()
                    .promptTokens(geminiResult.getUsageMetadata().getPromptTokenCount())
                    .candidatesTokens(geminiResult.getUsageMetadata().getCandidatesTokenCount())
                    .totalTokens(geminiResult.getUsageMetadata().getTotalTokenCount())
                    .build();
        }
        
        return AiAnalysisResponse.builder()
                .riskLevel(riskLevel)
                .riskFactors(riskFactors)
                .recommendations(recommendations)
                .analysisSource(analysisSource)
                .analyzedAt(LocalDateTime.now())
                .message("✅ Gemini API를 통한 AI 분석이 완료되었습니다.")
                .usage(usage)
                .build();
    }

    /**
     * 이미지 기반 위험 분석 (Mock)
     */
    public AiPhotoAnalysisResponse analyzePhoto(byte[] imageData, String filename) {
        log.info("[AI 이미지 분석 요청] filename={}, size={}bytes", filename, imageData != null ? imageData.length : 0);

        // Mock 응답 데이터 생성
        return AiPhotoAnalysisResponse.builder()
                .riskLevel("MEDIUM")
                .detectedIssues(List.of(
                    "[Mock] 사다리 고정 상태 불안정",
                    "[Mock] 작업 구역 조명 부족",
                    "[Mock] 안전 표지판 미설치"
                ))
                .tags(List.of(
                    "고소작업",
                    "야간작업", 
                    "철골작업",
                    "2층_비계"
                ))
                .analysisSource("MOCK_DATA (Gemini Vision API 연동 예정)")
                .analyzedAt(LocalDateTime.now())
                .message("✅ 백엔드 AI 이미지 분석 API 호출 성공! 현재 Mock 데이터가 반환되고 있습니다.")
                .build();
    }
}



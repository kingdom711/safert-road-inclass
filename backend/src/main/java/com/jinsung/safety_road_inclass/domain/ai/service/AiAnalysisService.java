package com.jinsung.safety_road_inclass.domain.ai.service;

import com.jinsung.safety_road_inclass.domain.ai.dto.AiAnalysisRequest;
import com.jinsung.safety_road_inclass.domain.ai.dto.AiAnalysisResponse;
import com.jinsung.safety_road_inclass.domain.ai.dto.AiPhotoAnalysisResponse;
import com.jinsung.safety_road_inclass.domain.ai.service.DocumentEnhancementService.DocumentEnhancementResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 분석 서비스
 * - Spring AI ChatModel을 사용하여 문서 초안을 보강
 * - 기존 Request/Response 포맷 유지
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiAnalysisService {

    private final DocumentEnhancementService documentEnhancementService;

    /**
     * 텍스트 기반 문서 보강 (Spring AI ChatModel 연동)
     * 
     * @param request AI 분석 요청 DTO (문서 초안 포함)
     * @return AI 분석 응답 DTO (보강된 문서 포함)
     */
    public AiAnalysisResponse analyzeText(AiAnalysisRequest request) {
        // DTO 검증 (Controller에서 @Valid로 이미 검증됨)
        log.info("[AI 문서 보강 요청] checklistId={}, contentLength={}", 
                 request.getChecklistId(), 
                 request.getContent() != null ? request.getContent().length() : 0);
        
        // DTO에서 문서 초안 추출
        String draftContent = request.getContent();
        Long checklistId = request.getChecklistId();
        
        // Spring AI ChatModel을 통한 문서 보강
        DocumentEnhancementResult enhancementResult = documentEnhancementService.enhanceDocument(draftContent);
        
        log.info("[AI 문서 보강 완료] checklistId={}, enhancedContentLength={}", 
                checklistId, 
                enhancementResult.getEnhancedContent() != null ? enhancementResult.getEnhancedContent().length() : 0);
        
        // 보강 결과를 AiAnalysisResponse DTO로 변환
        return convertToAiAnalysisResponse(enhancementResult, draftContent);
    }
    
    /**
     * DocumentEnhancementResult를 AiAnalysisResponse로 변환
     * - 기존 Response 구조 유지
     * - 보강된 문서 내용은 recommendations 필드에 포함
     */
    private AiAnalysisResponse convertToAiAnalysisResponse(
            DocumentEnhancementResult enhancementResult, 
            String originalContent) {
        
        // 보강된 문서 내용을 recommendations에 포함
        // (기존 구조 유지를 위해 recommendations 필드 활용)
        List<String> recommendations = new ArrayList<>();
        if (enhancementResult.getEnhancedContent() != null && 
            !enhancementResult.getEnhancedContent().isEmpty()) {
            // 보강된 내용을 여러 줄로 분할하여 recommendations에 추가
            String[] lines = enhancementResult.getEnhancedContent().split("\n");
            for (String line : lines) {
                if (line != null && !line.trim().isEmpty()) {
                    recommendations.add(line.trim());
                }
            }
            // 내용이 너무 길 경우 첫 10줄만 포함
            if (recommendations.size() > 10) {
                recommendations = recommendations.subList(0, 10);
            }
        }
        
        // riskFactors는 원본 내용의 요약으로 설정
        List<String> riskFactors = new ArrayList<>();
        if (originalContent != null && !originalContent.isEmpty()) {
            // 원본 내용의 첫 100자 요약
            String summary = originalContent.length() > 100 
                ? originalContent.substring(0, 100) + "..." 
                : originalContent;
            riskFactors.add("원본 문서: " + summary);
        }
        
        // analysisSource 구성
        String analysisSource = "Spring AI Gemini (문서 보강)";
        
        // 토큰 사용량 정보 변환
        AiAnalysisResponse.Usage usage = null;
        if (enhancementResult.getPromptTokens() != null || 
            enhancementResult.getGenerationTokens() != null) {
            usage = AiAnalysisResponse.Usage.builder()
                    .promptTokens(enhancementResult.getPromptTokens())
                    .candidatesTokens(enhancementResult.getGenerationTokens())
                    .totalTokens(enhancementResult.getTotalTokens())
                    .build();
        }
        
        return AiAnalysisResponse.builder()
                .riskLevel("N/A") // 문서 보강 기능이므로 위험 등급은 N/A
                .riskFactors(riskFactors)
                .recommendations(recommendations)
                .analysisSource(analysisSource)
                .analyzedAt(LocalDateTime.now())
                .message("✅ Spring AI Gemini를 통한 문서 보강이 완료되었습니다.")
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



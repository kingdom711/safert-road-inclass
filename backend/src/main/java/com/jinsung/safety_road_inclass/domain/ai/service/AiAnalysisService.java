package com.jinsung.safety_road_inclass.domain.ai.service;

import com.jinsung.safety_road_inclass.domain.ai.dto.AiAnalysisRequest;
import com.jinsung.safety_road_inclass.domain.ai.dto.AiAnalysisResponse;
import com.jinsung.safety_road_inclass.domain.ai.dto.AiPhotoAnalysisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 분석 서비스 (Mock 구현)
 * TODO: 추후 Gemini API 연동으로 교체
 */
@Service
@Slf4j
public class AiAnalysisService {

    /**
     * 텍스트 기반 위험 분석 (Mock)
     */
    public AiAnalysisResponse analyzeText(AiAnalysisRequest request) {
        log.info("[AI 분석 요청] checklistId={}, content={}", 
                 request.getChecklistId(), 
                 request.getContent() != null ? request.getContent().substring(0, Math.min(50, request.getContent().length())) + "..." : "null");

        // Mock 응답 데이터 생성
        return AiAnalysisResponse.builder()
                .riskLevel("HIGH")
                .riskFactors(List.of(
                    "[Mock] 안전모 미착용 가능성 감지",
                    "[Mock] 안전난간 불안정 위험",
                    "[Mock] 추락 사고 위험 높음"
                ))
                .recommendations(List.of(
                    "[Mock] 모든 작업자 안전모 착용 의무화",
                    "[Mock] 안전난간 재점검 및 보강 필요",
                    "[Mock] 안전대 체결 상태 확인 필수",
                    "[Mock] 작업 전 TBM(Tool Box Meeting) 실시 권장"
                ))
                .analysisSource("MOCK_DATA (Gemini API 연동 예정)")
                .analyzedAt(LocalDateTime.now())
                .message("✅ 백엔드 AI 분석 API 호출 성공! 현재 Mock 데이터가 반환되고 있습니다.")
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


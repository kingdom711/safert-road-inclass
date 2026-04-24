package com.jinsung.safety_road_inclass.domain.ai.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Gemini AI가 반환하는 위험 분석 결과 DTO.
 *
 * 고용노동부 고시 제2023-19호(사업장 위험성평가에 관한 지침)에서 요구하는
 * 위험성 추정(빈도×강도), 법적 근거, 통제 위계(Hierarchy of Controls)를 포함한다.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiAnalysisResult {

    // Legacy / summary 필드 (AI가 직접 반환)
    private String riskFactor;
    private String riskLevel; // CRITICAL | HIGH | MEDIUM | LOW
    private List<String> remediationSteps;
    private String referenceCode;

    // KRAS 6대 유해위험요인 분류
    // 기계적 | 전기적 | 화학적 | 생물학적 | 작업특성 | 인간공학 | 사회심리
    private String hazardClassification;

    // 불안전 상태(사물/환경) vs 불안전 행동(사람 행위) 구분
    private String unsafeCondition;
    private String unsafeAct;

    // 예상 재해 유형 (추락/협착/감전/화재/질식/끼임/베임/화상 등)
    private String possibleAccident;

    // 정량 위험성 평가 (L × S = R)
    private ScoreDetail severity;
    private ScoreDetail likelihood;
    private Integer riskScore;

    // 법적 근거
    private List<LegalReference> legalBasis;

    // KOSHA Guide 정식 코드 (예: G-2-2011)
    private String koshaGuide;

    // 통제 위계 (Hierarchy of Controls)
    private ControlMeasures controlMeasures;

    // 조치 책임자
    private String responsibleRole; // 현장소장 | 안전관리자 | 작업자 | 관리감독자

    // 권장 조치 완료 기한(일)
    private Integer dueDays;

    // AI 자체 신뢰도 (0.0 ~ 1.0)
    private Double confidence;

    // 토큰 사용량
    private UsageMetadata usageMetadata;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScoreDetail {
        private Integer score;
        private String rationale;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LegalReference {
        private String law;     // 산업안전보건법 / 산업안전보건기준에 관한 규칙 / 중대재해처벌법
        private String article; // 제42조
        private String content; // 조문 요지
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ControlMeasures {
        private List<String> immediate;       // 즉시 조치
        private List<String> engineering;     // 공학적 대책 (설비/방호장치)
        private List<String> administrative;  // 관리적 대책 (작업절차/허가제)
        private List<String> ppe;             // 개인보호구
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UsageMetadata {
        private Integer promptTokenCount;
        private Integer candidatesTokenCount;
        private Integer totalTokenCount;
    }
}

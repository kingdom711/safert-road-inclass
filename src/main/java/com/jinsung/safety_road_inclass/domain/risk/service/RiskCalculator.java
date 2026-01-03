package com.jinsung.safety_road_inclass.domain.risk.service;

import com.jinsung.safety_road_inclass.domain.risk.entity.RiskLevel;

/**
 * RiskCalculator - 위험도 계산 Domain Service (Utility Class)
 */
public class RiskCalculator {

    private RiskCalculator() {
        // Utility class - 인스턴스화 방지
    }

    /**
     * 위험도 점수 계산: 빈도 × 강도
     * @param frequency 빈도 (1-5)
     * @param severity 강도 (1-5)
     * @return 위험도 점수 (1-25)
     */
    public static int calculateScore(int frequency, int severity) {
        validateRange(frequency, "frequency");
        validateRange(severity, "severity");
        return frequency * severity;
    }

    /**
     * 위험도 레벨 판정
     * @param score 위험도 점수
     * @return 위험도 레벨
     */
    public static RiskLevel determineLevel(int score) {
        if (score <= 4) return RiskLevel.LOW;
        if (score <= 9) return RiskLevel.MEDIUM;
        if (score <= 15) return RiskLevel.HIGH;
        return RiskLevel.CRITICAL;
    }

    /**
     * 범위 검증 (1-5)
     */
    private static void validateRange(int value, String fieldName) {
        if (value < 1 || value > 5) {
            throw new IllegalArgumentException(
                fieldName + "는 1~5 사이의 값이어야 합니다: " + value);
        }
    }
}


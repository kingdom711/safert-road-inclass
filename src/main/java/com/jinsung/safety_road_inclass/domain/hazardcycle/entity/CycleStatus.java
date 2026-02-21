package com.jinsung.safety_road_inclass.domain.hazardcycle.entity;

/**
 * Hazard Cycle 상태
 */
public enum CycleStatus {
    HAZARD_REPORTED,   // 위험 사진 업로드 완료
    AI_ANALYZED,       // AI 분석 + 1단계 보상 완료
    ACTION_COMPLETED,  // 조치 완료 + 2단계 보상 완료
    EXPIRED,
    REJECTED
}

package com.jinsung.safety_road_inclass.domain.activity.entity;

/**
 * ActivityType - 활동 유형
 */
public enum ActivityType {
    ATTENDANCE_CHECK_IN,   // 출석 체크인
    POINTS_EARNED,         // 포인트 적립
    POINTS_SPENT,          // 포인트 사용
    CHECKLIST_COMPLETED,   // 체크리스트 완료
    RISK_ASSESSED,         // 위험성 평가 완료
    QUEST_COMPLETED,       // 퀘스트 완료
    HAZARD_CYCLE_REPORTED, // Hazard Cycle 생성/AI 분석 완료
    HAZARD_CYCLE_COMPLETED // Hazard Cycle 조치 완료
}

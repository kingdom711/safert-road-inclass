package com.jinsung.safety_road_inclass.domain.checklist.entity;

/**
 * ChecklistStatus - 체크리스트 상태 Enum
 */
public enum ChecklistStatus {
    DRAFT,      // 임시 저장
    SUBMITTED,  // 제출됨
    APPROVED,   // 승인됨
    REJECTED    // 반려됨
}


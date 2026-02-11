package com.jinsung.safety_road_inclass.domain.reward.entity;

/**
 * RewardStatus - 사용자 보상 상태
 */
public enum RewardStatus {
    PENDING, // 처리 대기중
    DELIVERED, // 발송 완료
    CANCELLED // 취소됨
}

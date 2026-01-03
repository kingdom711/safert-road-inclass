package com.jinsung.safety_road_inclass.domain.risk.entity;

/**
 * RiskLevel - 위험도 레벨 Enum
 */
public enum RiskLevel {
    LOW,      // 1-4: 허용 가능
    MEDIUM,   // 5-9: 관리 필요
    HIGH,     // 10-15: 즉시 조치 필요
    CRITICAL  // 16-25: 작업 중단 및 즉시 대책
}


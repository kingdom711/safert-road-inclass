package com.jinsung.safety_road_inclass.domain.workstop.entity;

/**
 * HazardType - 위험 유형 Enum
 *
 * 중대재해처벌법 제2조(정의) 기반 위험 유형 분류
 */
public enum HazardType {
    FALL("추락 위험"),
    PINCH("끼임 위험"),
    COLLISION("부딪힘 위험"),
    ELECTRIC("전기 위험"),
    FIRE("화재 위험"),
    CONFINED("밀폐공간 위험"),
    CHEMICAL("화학물질 위험"),
    COLLAPSE("붕괴 위험"),
    OTHER("기타");

    private final String label;

    HazardType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

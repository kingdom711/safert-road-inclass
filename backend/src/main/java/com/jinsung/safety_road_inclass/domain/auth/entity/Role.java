package com.jinsung.safety_road_inclass.domain.auth.entity;

/**
 * Role - 사용자 역할 Enum
 * 
 * Spring Security의 권한 체크를 위해 ROLE_ 접두어 사용
 */
public enum Role {
    
    ROLE_WORKER,         // 작업자 (기술인) - 체크리스트 작성
    ROLE_SUPERVISOR,     // 관리감독자 - 검토 및 승인
    ROLE_SAFETY_MANAGER; // 안전관리자 - 전체 현황 조회, 최종 승인

    /**
     * ROLE_ 접두어 제거한 이름 반환
     */
    public String getSimpleName() {
        return this.name().replace("ROLE_", "");
    }
}


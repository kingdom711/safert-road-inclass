package com.jinsung.safety_road_inclass.domain.gameprofile.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * GameProfileSyncRequest - localStorage → 서버 동기화 요청 DTO
 * 기존 localStorage 데이터를 서버로 마이그레이션할 때 사용
 */
@Getter
@NoArgsConstructor
public class GameProfileSyncRequest {

    // 프로필 데이터
    private int level = 1;
    private int exp = 0;
    private int expToNext = 100;
    private String gameRole;
    private String activeSpecialization;
    private int totalQuestsCompleted = 0;

    // 전직 데이터
    private List<SpecializationData> specializations;

    // 포인트 데이터
    private int pointsBalance = 0;

    // 스트릭 데이터
    private int currentStreak = 0;
    private int longestStreak = 0;
    private String lastCheckInDate;

    @Getter
    @NoArgsConstructor
    public static class SpecializationData {
        private String specId;
        private String unlockedAt;
        private String educationProgress;
    }
}

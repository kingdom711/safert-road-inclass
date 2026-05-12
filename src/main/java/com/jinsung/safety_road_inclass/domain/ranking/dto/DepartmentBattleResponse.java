package com.jinsung.safety_road_inclass.domain.ranking.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * DepartmentBattleResponse - 부서 대항전 응답 DTO
 *
 * 한 달간 부서(Team) 단위로 안전 점수를 집계하여 순위를 산정한다.
 * 점수 카테고리: 퀘스트 참여율, 교육 이수율, 위험 신고, 무재해, 체크리스트 제출률
 */
@Getter
@Builder
public class DepartmentBattleResponse {

    /** 대상 월 (예: "2026-05") */
    private String month;

    /** 이번 달 남은 일수 */
    private int remainingDays;

    /** 내 부서 ID (멤버십이 없으면 null) */
    private Long myDeptId;

    /** 부서 목록 (순위 정렬) */
    private List<DepartmentEntry> departments;

    /** 카테고리별 메타 (label, maxScore) */
    private Map<String, CategoryMeta> categories;

    /** 보상 테이블 */
    private List<RewardEntry> rewards;

    @Getter
    @Builder
    public static class DepartmentEntry {
        private Long id;
        private String name;
        private int memberCount;
        private int rank;
        private double totalScore;
        /** 카테고리별 실제 점수 (questParticipation, educationRate, hazardReports, safeDays, checklistRate) */
        private Map<String, Double> scores;
        /** 전월 대비 순위 변동 (현재 0 고정 - 과거 스냅샷 미보관) */
        private int rankChange;
    }

    @Getter
    @Builder
    public static class CategoryMeta {
        private String label;
        private double maxScore;
    }

    @Getter
    @Builder
    public static class RewardEntry {
        /** 1, 2, 3, 0(참여 부서) */
        private int rank;
        private String reward;
        private int points;
        private String icon;
    }
}

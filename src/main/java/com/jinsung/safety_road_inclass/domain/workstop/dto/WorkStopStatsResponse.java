package com.jinsung.safety_road_inclass.domain.workstop.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * 작업중지 통계 응답 — 컴플라이언스 리포트 연동용
 */
@Getter
@Builder
public class WorkStopStatsResponse {

    private long totalReports;
    private long recentReports;       // 최근 30일
    private long resolvedCount;
    private int resolutionRate;       // 해결율 (%)
    private long avgResponseMinutes;  // 평균 대응 시간 (분)
    private long retaliationCount;
    private long activeProtections;
    private Map<String, Long> byHazardType;
}

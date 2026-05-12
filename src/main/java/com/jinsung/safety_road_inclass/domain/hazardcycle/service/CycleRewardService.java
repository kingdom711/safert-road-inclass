package com.jinsung.safety_road_inclass.domain.hazardcycle.service;

import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.HazardReport;
import com.jinsung.safety_road_inclass.domain.point.dto.AddPointsResponse;
import com.jinsung.safety_road_inclass.domain.point.dto.PointsResponse;
import com.jinsung.safety_road_inclass.domain.point.service.PointService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * CycleRewardService - Hazard Cycle 2단계 보상 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CycleRewardService {

    private final PointService pointService;

    public static final int TIER1_POINTS = 100;
    public static final int TIER2_POINTS = 100;
    // 보너스 포인트 (구 골드 보상을 1:1 포인트로 통일)
    private static final int TIER1_BONUS_POINTS = 10;
    private static final int TIER2_BONUS_POINTS = 10;

    @Transactional
    public RewardGrant awardTier1(HazardReport report) {
        if (report.getTier1PointsAwarded() > 0) {
            PointsResponse points = pointService.getPoints(report.getReporter().getId());
            return new RewardGrant(report.getTier1PointsAwarded(), points.getBalance(), "이미 1단계 보상이 지급되었습니다.");
        }

        AddPointsResponse points = pointService.addPoints(
                report.getReporter().getId(),
                TIER1_POINTS,
                "Hazard Cycle 1단계 보상",
                "위험 발견 및 AI 분석 완료 보상");

        pointService.addPoints(
                report.getReporter().getId(),
                TIER1_BONUS_POINTS,
                "Hazard Cycle 1단계 보너스",
                "위험 발견 활동 보너스 포인트");

        report.awardTier1(TIER1_POINTS, LocalDateTime.now());

        log.info("Hazard Cycle 1단계 보상 지급: reportId={}, userId={}, points={}",
                report.getId(), report.getReporter().getId(), TIER1_POINTS);

        return new RewardGrant(TIER1_POINTS, points.getBalanceAfter(), "1단계 보상 지급 완료 (+100P)");
    }

    @Transactional
    public RewardGrant awardTier2(HazardReport report) {
        if (report.getTier2PointsAwarded() > 0) {
            PointsResponse points = pointService.getPoints(report.getReporter().getId());
            return new RewardGrant(report.getTier2PointsAwarded(), points.getBalance(), "이미 2단계 보상이 지급되었습니다.");
        }

        AddPointsResponse points = pointService.addPoints(
                report.getReporter().getId(),
                TIER2_POINTS,
                "Hazard Cycle 2단계 보상",
                "조치 완료 검증 보상");

        pointService.addPoints(
                report.getReporter().getId(),
                TIER2_BONUS_POINTS,
                "Hazard Cycle 2단계 보너스",
                "조치 완료 활동 보너스 포인트");

        report.awardTier2(TIER2_POINTS, LocalDateTime.now());

        log.info("Hazard Cycle 2단계 보상 지급: reportId={}, userId={}, points={}",
                report.getId(), report.getReporter().getId(), TIER2_POINTS);

        return new RewardGrant(TIER2_POINTS, points.getBalanceAfter(), "2단계 보상 지급 완료 (+100P)");
    }

    @Getter
    @AllArgsConstructor
    public static class RewardGrant {
        private int pointsAwarded;
        private int newBalance;
        private String message;
    }
}

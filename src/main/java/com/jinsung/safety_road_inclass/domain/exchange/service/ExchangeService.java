package com.jinsung.safety_road_inclass.domain.exchange.service;

import com.jinsung.safety_road_inclass.domain.exchange.dto.ExchangeRateResponse;
import com.jinsung.safety_road_inclass.domain.exchange.dto.ExchangeResponse;
import com.jinsung.safety_road_inclass.domain.gold.service.GoldService;
import com.jinsung.safety_road_inclass.domain.point.service.PointService;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ExchangeService - 포인트-골드 교환 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ExchangeService {

    private static final int POINTS_PER_GOLD = 1000; // 1,000P = 1G

    private final PointService pointService;
    private final GoldService goldService;

    /**
     * 포인트를 골드로 교환
     */
    @Transactional
    public ExchangeResponse exchangePointsToGold(Long userId, int pointsAmount) {
        // 1. 교환 비율 검증
        if (pointsAmount < POINTS_PER_GOLD) {
            throw new CustomException(ErrorCode.EXCHANGE_MINIMUM_NOT_MET);
        }
        if (pointsAmount % POINTS_PER_GOLD != 0) {
            throw new CustomException(ErrorCode.EXCHANGE_INVALID_AMOUNT);
        }

        int goldAmount = pointsAmount / POINTS_PER_GOLD;

        // 2. 포인트 차감
        pointService.spendPoints(userId, pointsAmount, "골드 교환",
                goldAmount + "G 교환");

        // 3. 골드 적립
        goldService.addGold(userId, goldAmount, "포인트 교환",
                pointsAmount + "P 교환");

        log.info("Points exchanged to gold: userId={}, points={}, gold={}",
                userId, pointsAmount, goldAmount);

        return ExchangeResponse.builder()
                .pointsSpent(pointsAmount)
                .goldEarned(goldAmount)
                .exchangeRate(POINTS_PER_GOLD)
                .build();
    }

    /**
     * 교환 비율 조회
     */
    public ExchangeRateResponse getExchangeRate() {
        return ExchangeRateResponse.builder()
                .pointsPerGold(POINTS_PER_GOLD)
                .minimumPoints(POINTS_PER_GOLD)
                .build();
    }
}

package com.jinsung.safety_road_inclass.domain.quest.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PeriodKeyResolverTest {

    @Test
    @DisplayName("dailyKey: KST 날짜를 yyyyMMdd로 변환")
    void dailyKey() {
        assertThat(PeriodKeyResolver.dailyKey(LocalDateTime.of(2026, 4, 29, 0, 0)))
                .isEqualTo("20260429");
    }

    @Test
    @DisplayName("weeklyKey: ISO 주차는 월요일을 주 시작으로 사용")
    void weeklyKey_mondayBoundary() {
        assertThat(PeriodKeyResolver.weeklyKey(LocalDateTime.of(2026, 1, 5, 0, 0)))
                .isEqualTo("2026-W02");
    }

    @Test
    @DisplayName("weeklyKey: 연도 경계에서도 ISO week-based-year 사용")
    void weeklyKey_yearBoundary() {
        assertThat(PeriodKeyResolver.weeklyKey(LocalDateTime.of(2027, 1, 1, 0, 0)))
                .isEqualTo("2026-W53");
    }
}

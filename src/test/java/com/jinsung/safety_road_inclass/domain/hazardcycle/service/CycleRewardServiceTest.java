package com.jinsung.safety_road_inclass.domain.hazardcycle.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.gold.service.GoldService;
import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.HazardReport;
import com.jinsung.safety_road_inclass.domain.point.dto.AddPointsResponse;
import com.jinsung.safety_road_inclass.domain.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CycleRewardService 단위 테스트")
class CycleRewardServiceTest {

    @Mock
    private PointService pointService;

    @Mock
    private GoldService goldService;

    @InjectMocks
    private CycleRewardService cycleRewardService;

    @Test
    @DisplayName("awardTier1_포인트정확성_검증")
    void awardTier1_포인트정확성_검증() {
        User user = User.builder()
                .username("worker1")
                .password("pw")
                .name("작업자")
                .role(Role.ROLE_WORKER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        HazardReport report = HazardReport.builder()
                .reporter(user)
                .hazardPhotoPath("photo.jpg")
                .build();

        when(pointService.addPoints(eq(1L), eq(100), anyString(), anyString()))
                .thenReturn(AddPointsResponse.builder()
                        .amount(100)
                        .balanceAfter(1100)
                        .reason("Hazard Cycle 1단계 보상")
                        .build());

        CycleRewardService.RewardGrant reward = cycleRewardService.awardTier1(report);

        assertThat(reward.getPointsAwarded()).isEqualTo(100);
        assertThat(report.getTier1PointsAwarded()).isEqualTo(100);
        assertThat(report.getTier1AwardedAt()).isNotNull();
    }

    @Test
    @DisplayName("awardTier2_총보상_TIER1의2배_검증")
    void awardTier2_총보상_TIER1의2배_검증() {
        User user = User.builder()
                .username("worker1")
                .password("pw")
                .name("작업자")
                .role(Role.ROLE_WORKER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        HazardReport report = HazardReport.builder()
                .reporter(user)
                .hazardPhotoPath("photo.jpg")
                .build();

        when(pointService.addPoints(eq(1L), eq(100), anyString(), anyString()))
                .thenReturn(AddPointsResponse.builder()
                        .amount(100)
                        .balanceAfter(1200)
                        .reason("Hazard Cycle 보상")
                        .build());

        cycleRewardService.awardTier1(report);
        cycleRewardService.awardTier2(report);

        assertThat(report.getTier1PointsAwarded()).isEqualTo(100);
        assertThat(report.getTier2PointsAwarded()).isEqualTo(100);
        assertThat(report.getTotalPointsAwarded()).isEqualTo(200);
    }
}

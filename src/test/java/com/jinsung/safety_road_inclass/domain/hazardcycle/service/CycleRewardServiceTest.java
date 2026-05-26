package com.jinsung.safety_road_inclass.domain.hazardcycle.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CycleRewardService tests")
class CycleRewardServiceTest {

    @Mock
    private PointService pointService;

    @InjectMocks
    private CycleRewardService cycleRewardService;

    @Test
    @DisplayName("awardTier1 stores the combined point reward")
    void awardTier1StoresCombinedReward() {
        User user = User.builder()
                .username("worker1")
                .password("pw")
                .name("worker")
                .role(Role.ROLE_WORKER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        HazardReport report = HazardReport.builder()
                .reporter(user)
                .hazardPhotoPath("photo.jpg")
                .build();

        when(pointService.addPoints(eq(1L), anyInt(), anyString(), anyString()))
                .thenAnswer(invocation -> AddPointsResponse.builder()
                        .amount(invocation.getArgument(1, Integer.class))
                        .balanceAfter(1000 + invocation.getArgument(1, Integer.class))
                        .reason(invocation.getArgument(2, String.class))
                        .build());

        CycleRewardService.RewardGrant reward = cycleRewardService.awardTier1(report);

        assertThat(reward.getPointsAwarded()).isEqualTo(110);
        assertThat(reward.getNewBalance()).isEqualTo(1010);
        assertThat(report.getTier1PointsAwarded()).isEqualTo(110);
        assertThat(report.getTier1AwardedAt()).isNotNull();
        verify(pointService, times(2)).addPoints(eq(1L), anyInt(), anyString(), anyString());
    }

    @Test
    @DisplayName("awardTier2 keeps combined totals across both tiers")
    void awardTier2KeepsCombinedTotalsAcrossBothTiers() {
        User user = User.builder()
                .username("worker1")
                .password("pw")
                .name("worker")
                .role(Role.ROLE_WORKER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        HazardReport report = HazardReport.builder()
                .reporter(user)
                .hazardPhotoPath("photo.jpg")
                .build();

        when(pointService.addPoints(eq(1L), anyInt(), anyString(), anyString()))
                .thenAnswer(invocation -> AddPointsResponse.builder()
                        .amount(invocation.getArgument(1, Integer.class))
                        .balanceAfter(1100 + invocation.getArgument(1, Integer.class))
                        .reason(invocation.getArgument(2, String.class))
                        .build());

        cycleRewardService.awardTier1(report);
        cycleRewardService.awardTier2(report);

        assertThat(report.getTier1PointsAwarded()).isEqualTo(110);
        assertThat(report.getTier2PointsAwarded()).isEqualTo(110);
        assertThat(report.getTotalPointsAwarded()).isEqualTo(220);
    }
}

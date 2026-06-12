package com.jinsung.safety_road_inclass.domain.admin.service;

import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.checklist.repository.ChecklistRepository;
import com.jinsung.safety_road_inclass.domain.education.repository.EducationCompletionRepository;
import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.HazardReport;
import com.jinsung.safety_road_inclass.domain.hazardcycle.repository.HazardReportRepository;
import com.jinsung.safety_road_inclass.domain.reward.entity.RewardStatus;
import com.jinsung.safety_road_inclass.domain.reward.repository.UserRewardRepository;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamRepository;
import com.jinsung.safety_road_inclass.domain.workstop.repository.WorkStopReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private EducationCompletionRepository educationCompletionRepository;

    @Mock
    private ChecklistRepository checklistRepository;

    @Mock
    private HazardReportRepository hazardReportRepository;

    @Mock
    private WorkStopReportRepository workStopReportRepository;

    @Mock
    private UserRewardRepository userRewardRepository;

    private AdminDashboardService adminDashboardService;

    @BeforeEach
    void setUp() {
        adminDashboardService = new AdminDashboardService(
                userRepository,
                teamRepository,
                educationCompletionRepository,
                checklistRepository,
                hazardReportRepository,
                workStopReportRepository,
                userRewardRepository
        );
    }

    @Test
    @DisplayName("getSummary: totalUsers detail query fails, other metrics still return")
    void getSummary_whenParticipantDetailsFail_keepsOtherMetrics() {
        when(userRepository.findParticipantRows()).thenThrow(new RuntimeException("boom"));
        when(teamRepository.count()).thenReturn(7L);
        when(educationCompletionRepository.countByCompletedAtBetween(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(3L);
        when(checklistRepository.countByCreatedAtBetween(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(5L);
        when(hazardReportRepository.countByReportedAtBetween(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(2L);
        when(workStopReportRepository.countByStatusNotIn(org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(4L);
        when(userRewardRepository.countByStatus(RewardStatus.PENDING)).thenReturn(1L);
        when(hazardReportRepository.countByStatusIn(org.mockito.ArgumentMatchers.anyList())).thenReturn(6L);

        when(educationCompletionRepository.findByPeriod(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(Collections.emptyList());
        when(checklistRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(Collections.emptyList());
        when(hazardReportRepository.findByStatusInOrderByReportedAtDesc(org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(Collections.emptyList());
        when(workStopReportRepository.findByStatusNotInOrderByCreatedAtDesc(org.mockito.ArgumentMatchers.anyList()))
                .thenReturn(Collections.emptyList());
        when(userRewardRepository.findWithRewardByStatusOrderByCreatedAtAsc(RewardStatus.PENDING))
                .thenReturn(Collections.emptyList());
        when(workStopReportRepository.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.emptyList());
        when(userRewardRepository.findByStatusOrderByCreatedAtAsc(RewardStatus.PENDING)).thenReturn(Collections.emptyList());
        when(checklistRepository.findWithRiskItems(org.mockito.ArgumentMatchers.any())).thenReturn(Collections.emptyList());
        Page<HazardReport> emptyPage = new PageImpl<>(List.of());
        when(hazardReportRepository.findAllByOrderByReportedAtDesc(org.mockito.ArgumentMatchers.any()))
                .thenReturn(emptyPage);
        when(workStopReportRepository.countByHazardTypeAndPeriod(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(Collections.emptyList());

        var summary = adminDashboardService.getSummary();

        assertThat(summary.getMetrics().getTotalUsers()).isZero();
        assertThat(summary.getMetrics().getTotalTeams()).isEqualTo(7L);
        assertThat(summary.getMetrics().getTodayEducationCompletions()).isEqualTo(3L);
        assertThat(summary.getMetrics().getTodayChecklistSubmissions()).isEqualTo(5L);
        assertThat(summary.getMetrics().getTodayHazardReports()).isEqualTo(2L);
        assertThat(summary.getMetrics().getOpenWorkStopReports()).isEqualTo(4L);
        assertThat(summary.getMetrics().getPendingRewardRequests()).isEqualTo(1L);
        assertThat(summary.getMetrics().getOpenHazardCycles()).isEqualTo(6L);
        assertThat(summary.getMetricDetails()).containsKey("totalUsers");
        assertThat(summary.getMetricDetails().get("totalUsers")).isEmpty();
    }
}

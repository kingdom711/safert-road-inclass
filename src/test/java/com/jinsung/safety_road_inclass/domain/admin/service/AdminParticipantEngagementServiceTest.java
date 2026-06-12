package com.jinsung.safety_road_inclass.domain.admin.service;

import com.jinsung.safety_road_inclass.domain.attendance.repository.AttendanceRecordRepository;
import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.education.repository.EducationCompletionRepository;
import com.jinsung.safety_road_inclass.domain.hazardcycle.repository.HazardReportAckRepository;
import com.jinsung.safety_road_inclass.domain.hazardcycle.repository.HazardReportRepository;
import com.jinsung.safety_road_inclass.domain.point.entity.TransactionType;
import com.jinsung.safety_road_inclass.domain.point.repository.PointTransactionRepository;
import com.jinsung.safety_road_inclass.domain.quest.repository.QuestProgressRepository;
import com.jinsung.safety_road_inclass.domain.workstop.repository.WorkStopReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminParticipantEngagementServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AttendanceRecordRepository attendanceRecordRepository;

    @Mock
    private EducationCompletionRepository educationCompletionRepository;

    @Mock
    private QuestProgressRepository questProgressRepository;

    @Mock
    private HazardReportRepository hazardReportRepository;

    @Mock
    private HazardReportAckRepository hazardReportAckRepository;

    @Mock
    private WorkStopReportRepository workStopReportRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    private AdminParticipantEngagementService service;

    @BeforeEach
    void setUp() {
        service = new AdminParticipantEngagementService(
                userRepository,
                attendanceRecordRepository,
                educationCompletionRepository,
                questProgressRepository,
                hazardReportRepository,
                hazardReportAckRepository,
                workStopReportRepository,
                pointTransactionRepository
        );
    }

    @Test
    @DisplayName("getParticipantEngagement: combines per-user activity aggregates")
    void getParticipantEngagement_combinesAggregates() {
        User worker = User.builder()
                .username("worker1")
                .password("pw")
                .role(Role.ROLE_WORKER)
                .name("김현장")
                .email("worker1@example.com")
                .isVerified(true)
                .build();
        ReflectionTestUtils.setField(worker, "id", 7L);

        when(userRepository.findParticipants(any())).thenReturn(List.of(worker));
        when(attendanceRecordRepository.aggregateByUserAndCheckInDateBetween(any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{7L, 2L, LocalDate.of(2026, 6, 10)}));
        when(educationCompletionRepository.aggregateByUserAndCompletedAtBetween(any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{7L, 1L, 87.5, LocalDateTime.of(2026, 6, 9, 8, 30)}));
        when(questProgressRepository.aggregateByUserAndActivityBetween(any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{7L, 3L, 4L, LocalDateTime.of(2026, 6, 11, 12, 0)}));
        when(hazardReportRepository.aggregateByReporterAndReportedAtBetween(any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{7L, 1L, LocalDateTime.of(2026, 6, 8, 14, 0)}));
        when(hazardReportAckRepository.aggregateByAckerAndAckedAtBetween(any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{7L, 2L, LocalDateTime.of(2026, 6, 12, 9, 0)}));
        when(workStopReportRepository.aggregateByReporterAndCreatedAtBetween(any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{7L, 1L, LocalDateTime.of(2026, 6, 7, 9, 0)}));
        when(pointTransactionRepository.aggregateAmountByUserAndTypeAndCreatedAtBetween(eq(TransactionType.EARN), any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{7L, 1200L, LocalDateTime.of(2026, 6, 11, 15, 0)}));

        var response = service.getParticipantEngagement(
                LocalDate.of(2026, 6, 1),
                LocalDate.of(2026, 6, 30),
                null,
                null,
                "score"
        );

        assertThat(response.getParticipants()).hasSize(1);
        var row = response.getParticipants().get(0);
        assertThat(row.getUserId()).isEqualTo(7L);
        assertThat(row.getName()).isEqualTo("김현장");
        assertThat(row.getAttendanceCount()).isEqualTo(2);
        assertThat(row.getEducationCompletions()).isEqualTo(1);
        assertThat(row.getAverageQuizScore()).isEqualTo(87.5);
        assertThat(row.getQuestCompletions()).isEqualTo(3);
        assertThat(row.getHazardReports()).isEqualTo(1);
        assertThat(row.getHazardAcks()).isEqualTo(2);
        assertThat(row.getWorkStopReports()).isEqualTo(1);
        assertThat(row.getPointsEarned()).isEqualTo(1200);
        assertThat(row.getEngagementScore()).isEqualTo(62);
        assertThat(row.getLastActivityAt()).isEqualTo(LocalDateTime.of(2026, 6, 12, 9, 0));

        assertThat(response.getSummary().getTotalParticipants()).isEqualTo(1);
        assertThat(response.getSummary().getActiveParticipants()).isEqualTo(1);
        assertThat(response.getSummary().getAverageEngagementScore()).isEqualTo(62.0);
        assertThat(response.getSummary().getTotalEducationCompletions()).isEqualTo(1);
        assertThat(response.getSummary().getTotalHazardReports()).isEqualTo(1);
        assertThat(response.getSummary().getTotalWorkStopReports()).isEqualTo(1);
        assertThat(response.getSummary().getTotalPointsEarned()).isEqualTo(1200);
    }
}

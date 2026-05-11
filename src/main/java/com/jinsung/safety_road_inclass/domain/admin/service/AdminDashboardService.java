package com.jinsung.safety_road_inclass.domain.admin.service;

import com.jinsung.safety_road_inclass.domain.admin.dto.AdminDashboardSummaryResponse;
import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.checklist.entity.ChecklistStatus;
import com.jinsung.safety_road_inclass.domain.checklist.repository.ChecklistRepository;
import com.jinsung.safety_road_inclass.domain.education.repository.EducationCompletionRepository;
import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.CycleStatus;
import com.jinsung.safety_road_inclass.domain.hazardcycle.repository.HazardReportRepository;
import com.jinsung.safety_road_inclass.domain.reward.entity.RewardStatus;
import com.jinsung.safety_road_inclass.domain.reward.repository.UserRewardRepository;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamRepository;
import com.jinsung.safety_road_inclass.domain.workstop.entity.ReportStatus;
import com.jinsung.safety_road_inclass.domain.workstop.repository.WorkStopReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private static final List<ReportStatus> CLOSED_WORK_STOP_STATUSES =
            List.of(ReportStatus.RESOLVED, ReportStatus.RESUMED);

    private static final List<CycleStatus> OPEN_HAZARD_STATUSES =
            List.of(CycleStatus.HAZARD_REPORTED, CycleStatus.AI_ANALYZED);

    private static final List<Role> NON_PARTICIPANT_ROLES =
            List.of(Role.ROLE_ADMIN, Role.ROLE_PROJECT_ADMIN);

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final EducationCompletionRepository educationCompletionRepository;
    private final ChecklistRepository checklistRepository;
    private final HazardReportRepository hazardReportRepository;
    private final WorkStopReportRepository workStopReportRepository;
    private final UserRewardRepository userRewardRepository;

    public AdminDashboardSummaryResponse getSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

        var metrics = AdminDashboardSummaryResponse.Metrics.builder()
                .totalUsers(userRepository.countParticipants(NON_PARTICIPANT_ROLES))
                .totalTeams(teamRepository.count())
                .todayEducationCompletions(educationCompletionRepository.countByCompletedAtBetween(todayStart, todayEnd))
                .todayChecklistSubmissions(checklistRepository.countByCreatedAtBetween(todayStart, todayEnd))
                .todayHazardReports(hazardReportRepository.countByReportedAtBetween(todayStart, todayEnd))
                .openWorkStopReports(workStopReportRepository.countByStatusNotIn(CLOSED_WORK_STOP_STATUSES))
                .pendingRewardRequests(userRewardRepository.countByStatus(RewardStatus.PENDING))
                .openHazardCycles(hazardReportRepository.countByStatusIn(OPEN_HAZARD_STATUSES))
                .build();

        return AdminDashboardSummaryResponse.builder()
                .generatedAt(now)
                .metrics(metrics)
                .actionItems(buildActionItems())
                .recentActivities(buildRecentActivities())
                .workStopByHazardType(loadWorkStopHazardStats(todayStart.minusDays(30), todayEnd))
                .build();
    }

    private List<AdminDashboardSummaryResponse.ActionItem> buildActionItems() {
        List<AdminDashboardSummaryResponse.ActionItem> items = new ArrayList<>();

        workStopReportRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(report -> !CLOSED_WORK_STOP_STATUSES.contains(report.getStatus()))
                .limit(5)
                .map(report -> AdminDashboardSummaryResponse.ActionItem.builder()
                        .type("WORK_STOP")
                        .title("작업중지 신고 확인")
                        .detail(report.getZone() + " / " + report.getHazardType().getLabel())
                        .status(report.getStatus().name())
                        .href("/work-stop-history")
                        .createdAt(report.getCreatedAt())
                        .build())
                .forEach(items::add);

        userRewardRepository.findByStatusOrderByCreatedAtAsc(RewardStatus.PENDING).stream()
                .limit(5)
                .map(reward -> AdminDashboardSummaryResponse.ActionItem.builder()
                        .type("REWARD")
                        .title("보상 교환 승인")
                        .detail(reward.getUser().getName() + " / " + reward.getReward().getName())
                        .status(reward.getStatus().name())
                        .href("/admin/reward-approval")
                        .createdAt(reward.getCreatedAt())
                        .build())
                .forEach(items::add);

        checklistRepository.findWithRiskItems(ChecklistStatus.SUBMITTED).stream()
                .limit(5)
                .map(checklist -> AdminDashboardSummaryResponse.ActionItem.builder()
                        .type("CHECKLIST")
                        .title("위험 항목 체크리스트 검토")
                        .detail(checklist.getSiteName() + " / " + checklist.getCreatedBy().getName())
                        .status(checklist.getStatus().name())
                        .href("/risk-solution")
                        .createdAt(checklist.getCreatedAt())
                        .build())
                .forEach(items::add);

        return items.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .toList();
    }

    private List<AdminDashboardSummaryResponse.RecentActivity> buildRecentActivities() {
        List<AdminDashboardSummaryResponse.RecentActivity> items = new ArrayList<>();

        hazardReportRepository.findAllByOrderByReportedAtDesc(PageRequest.of(0, 5)).stream()
                .map(report -> AdminDashboardSummaryResponse.RecentActivity.builder()
                        .type("HAZARD")
                        .title("위험요소 신고")
                        .detail(report.getLocationDescription())
                        .occurredAt(report.getReportedAt())
                        .build())
                .forEach(items::add);

        workStopReportRepository.findAllByOrderByCreatedAtDesc().stream()
                .limit(5)
                .map(report -> AdminDashboardSummaryResponse.RecentActivity.builder()
                        .type("WORK_STOP")
                        .title("작업중지 신고")
                        .detail(report.getZone() + " / " + report.getStatus().getLabel())
                        .occurredAt(report.getCreatedAt())
                        .build())
                .forEach(items::add);

        return items.stream()
                .sorted((a, b) -> b.getOccurredAt().compareTo(a.getOccurredAt()))
                .limit(10)
                .toList();
    }

    private Map<String, Long> loadWorkStopHazardStats(LocalDateTime start, LocalDateTime end) {
        return workStopReportRepository.countByHazardTypeAndPeriod(start, end).stream()
                .collect(Collectors.toMap(
                        row -> String.valueOf(row[0]),
                        row -> (Long) row[1]
                ));
    }
}

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
import com.jinsung.safety_road_inclass.domain.workstop.entity.HazardType;
import com.jinsung.safety_road_inclass.domain.workstop.entity.ReportStatus;
import com.jinsung.safety_road_inclass.domain.workstop.repository.WorkStopReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
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
        try {
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);

            var metrics = AdminDashboardSummaryResponse.Metrics.builder()
                    .totalUsers(safeCount("total users", () -> userRepository.countParticipants(NON_PARTICIPANT_ROLES)))
                    .totalTeams(safeCount("total teams", teamRepository::count))
                    .todayEducationCompletions(safeCount("today education completions",
                            () -> educationCompletionRepository.countByCompletedAtBetween(todayStart, todayEnd)))
                    .todayChecklistSubmissions(safeCount("today checklist submissions",
                            () -> checklistRepository.countByCreatedAtBetween(todayStart, todayEnd)))
                    .todayHazardReports(safeCount("today hazard reports",
                            () -> hazardReportRepository.countByReportedAtBetween(todayStart, todayEnd)))
                    .openWorkStopReports(safeCount("open work-stop reports",
                            () -> workStopReportRepository.countByStatusNotIn(CLOSED_WORK_STOP_STATUSES)))
                    .pendingRewardRequests(safeCount("pending reward requests",
                            () -> userRewardRepository.countByStatus(RewardStatus.PENDING)))
                    .openHazardCycles(safeCount("open hazard cycles",
                            () -> hazardReportRepository.countByStatusIn(OPEN_HAZARD_STATUSES)))
                    .build();

            return AdminDashboardSummaryResponse.builder()
                    .generatedAt(now)
                    .metrics(metrics)
                    .actionItems(buildActionItems())
                    .recentActivities(buildRecentActivities())
                    .workStopByHazardType(loadWorkStopHazardStats(todayStart.minusDays(30), todayEnd))
                    .build();
        } catch (Exception e) {
            log.error("Admin dashboard summary failed; returning empty summary", e);
            return emptySummary(now);
        }
    }

    private List<AdminDashboardSummaryResponse.ActionItem> buildActionItems() {
        List<AdminDashboardSummaryResponse.ActionItem> items = new ArrayList<>();

        safeList("work-stop action items", workStopReportRepository::findAllByOrderByCreatedAtDesc).stream()
                .filter(report -> !CLOSED_WORK_STOP_STATUSES.contains(report.getStatus()))
                .limit(5)
                .map(report -> AdminDashboardSummaryResponse.ActionItem.builder()
                        .type("WORK_STOP")
                        .title("작업중지 신고 확인")
                        .detail(joinDetail(report.getZone(), labelOf(report.getHazardType())))
                        .status(nameOf(report.getStatus()))
                        .href("/work-stop-history")
                        .createdAt(report.getCreatedAt())
                        .build())
                .forEach(items::add);

        safeList("reward action items",
                () -> userRewardRepository.findByStatusOrderByCreatedAtAsc(RewardStatus.PENDING)).stream()
                .limit(5)
                .map(reward -> AdminDashboardSummaryResponse.ActionItem.builder()
                        .type("REWARD")
                        .title("보상 교환 승인")
                        .detail(joinDetail(
                                reward.getUser() == null ? null : reward.getUser().getName(),
                                reward.getReward() == null ? null : reward.getReward().getName()))
                        .status(nameOf(reward.getStatus()))
                        .href("/admin/reward-approval")
                        .createdAt(reward.getCreatedAt())
                        .build())
                .forEach(items::add);

        safeList("checklist action items",
                () -> checklistRepository.findWithRiskItems(ChecklistStatus.SUBMITTED)).stream()
                .limit(5)
                .map(checklist -> AdminDashboardSummaryResponse.ActionItem.builder()
                        .type("CHECKLIST")
                        .title("위험 항목 체크리스트 검토")
                        .detail(joinDetail(
                                checklist.getSiteName(),
                                checklist.getCreatedBy() == null ? null : checklist.getCreatedBy().getName()))
                        .status(nameOf(checklist.getStatus()))
                        .href("/risk-solution")
                        .createdAt(checklist.getCreatedAt())
                        .build())
                .forEach(items::add);

        return items.stream()
                .filter(item -> item.getCreatedAt() != null)
                .sorted(Comparator.comparing(AdminDashboardSummaryResponse.ActionItem::getCreatedAt).reversed())
                .limit(10)
                .toList();
    }

    private List<AdminDashboardSummaryResponse.RecentActivity> buildRecentActivities() {
        List<AdminDashboardSummaryResponse.RecentActivity> items = new ArrayList<>();

        safeList("hazard recent activities",
                () -> hazardReportRepository.findAllByOrderByReportedAtDesc(PageRequest.of(0, 5)).getContent()).stream()
                .map(report -> AdminDashboardSummaryResponse.RecentActivity.builder()
                        .type("HAZARD")
                        .title("위험요소 신고")
                        .detail(report.getLocationDescription())
                        .occurredAt(report.getReportedAt())
                        .build())
                .forEach(items::add);

        safeList("work-stop recent activities", workStopReportRepository::findAllByOrderByCreatedAtDesc).stream()
                .limit(5)
                .map(report -> AdminDashboardSummaryResponse.RecentActivity.builder()
                        .type("WORK_STOP")
                        .title("작업중지 신고")
                        .detail(joinDetail(report.getZone(), labelOf(report.getStatus())))
                        .occurredAt(report.getCreatedAt())
                        .build())
                .forEach(items::add);

        return items.stream()
                .filter(item -> item.getOccurredAt() != null)
                .sorted(Comparator.comparing(AdminDashboardSummaryResponse.RecentActivity::getOccurredAt).reversed())
                .limit(10)
                .toList();
    }

    private Map<String, Long> loadWorkStopHazardStats(LocalDateTime start, LocalDateTime end) {
        return safeList("work-stop hazard stats",
                () -> workStopReportRepository.countByHazardTypeAndPeriod(start, end)).stream()
                .filter(row -> row != null && row.length >= 2 && row[0] != null && row[1] instanceof Number)
                .collect(Collectors.toMap(
                        row -> nameOf(row[0]),
                        row -> ((Number) row[1]).longValue(),
                        Long::sum,
                        LinkedHashMap::new
                ));
    }

    private long safeCount(String label, Supplier<Long> supplier) {
        try {
            Long count = supplier.get();
            return count == null ? 0L : count;
        } catch (Exception e) {
            log.warn("Admin dashboard count failed: {}", label, e);
            return 0L;
        }
    }

    private <T> List<T> safeList(String label, Supplier<List<T>> supplier) {
        try {
            List<T> values = supplier.get();
            return values == null ? Collections.emptyList() : values;
        } catch (Exception e) {
            log.warn("Admin dashboard list failed: {}", label, e);
            return Collections.emptyList();
        }
    }

    private String joinDetail(String first, String second) {
        return List.of(first, second).stream()
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(" / "));
    }

    private String nameOf(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        return String.valueOf(value);
    }

    private String labelOf(Object value) {
        if (value instanceof ReportStatus status) {
            return status.getLabel();
        }
        if (value instanceof HazardType hazardType) {
            return hazardType.getLabel();
        }
        return nameOf(value);
    }

    private AdminDashboardSummaryResponse emptySummary(LocalDateTime generatedAt) {
        return AdminDashboardSummaryResponse.builder()
                .generatedAt(generatedAt)
                .metrics(AdminDashboardSummaryResponse.Metrics.builder()
                        .totalUsers(0)
                        .totalTeams(0)
                        .todayEducationCompletions(0)
                        .todayChecklistSubmissions(0)
                        .todayHazardReports(0)
                        .openWorkStopReports(0)
                        .pendingRewardRequests(0)
                        .openHazardCycles(0)
                        .build())
                .actionItems(Collections.emptyList())
                .recentActivities(Collections.emptyList())
                .workStopByHazardType(Collections.emptyMap())
                .build();
    }
}

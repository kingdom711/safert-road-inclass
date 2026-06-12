package com.jinsung.safety_road_inclass.domain.admin.service;

import com.jinsung.safety_road_inclass.domain.admin.dto.AdminDashboardSummaryResponse;
import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.checklist.entity.Checklist;
import com.jinsung.safety_road_inclass.domain.checklist.entity.ChecklistStatus;
import com.jinsung.safety_road_inclass.domain.checklist.repository.ChecklistRepository;
import com.jinsung.safety_road_inclass.domain.education.entity.EducationCompletion;
import com.jinsung.safety_road_inclass.domain.education.repository.EducationCompletionRepository;
import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.CycleStatus;
import com.jinsung.safety_road_inclass.domain.hazardcycle.entity.HazardReport;
import com.jinsung.safety_road_inclass.domain.hazardcycle.repository.HazardReportRepository;
import com.jinsung.safety_road_inclass.domain.reward.entity.RewardStatus;
import com.jinsung.safety_road_inclass.domain.reward.entity.UserReward;
import com.jinsung.safety_road_inclass.domain.reward.repository.UserRewardRepository;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamRepository;
import com.jinsung.safety_road_inclass.domain.workstop.entity.HazardType;
import com.jinsung.safety_road_inclass.domain.workstop.entity.ReportStatus;
import com.jinsung.safety_road_inclass.domain.workstop.entity.WorkStopReport;
import com.jinsung.safety_road_inclass.domain.workstop.repository.WorkStopReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
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

    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getSummary() {
        LocalDateTime now = LocalDateTime.now();
        try {
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
            Map<String, List<AdminDashboardSummaryResponse.MetricDetail>> metricDetails =
                    buildMetricDetails(todayStart, todayEnd);

            var metrics = AdminDashboardSummaryResponse.Metrics.builder()
                    .totalUsers(metricDetails.getOrDefault("totalUsers", Collections.emptyList()).size())
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
                    .metricDetails(metricDetails)
                    .actionItems(buildActionItems())
                    .recentActivities(buildRecentActivities())
                    .workStopByHazardType(loadWorkStopHazardStats(todayStart.minusDays(30), todayEnd))
                    .build();
        } catch (Exception e) {
            log.error("Admin dashboard summary failed; returning empty summary", e);
            return emptySummary(now);
        }
    }

    private Map<String, List<AdminDashboardSummaryResponse.MetricDetail>> buildMetricDetails(
            LocalDateTime todayStart,
            LocalDateTime todayEnd
    ) {
        Map<String, List<AdminDashboardSummaryResponse.MetricDetail>> details = new LinkedHashMap<>();

        details.put("totalUsers", safeList("total user details", userRepository::findParticipantRows).stream()
                .map(this::toUserDetail)
                .toList());

        details.put("todayEducationCompletions", safeList("today education completion details",
                () -> educationCompletionRepository.findByPeriod(todayStart, todayEnd)).stream()
                .map(this::toEducationDetail)
                .toList());

        details.put("todayChecklistSubmissions", safeList("today checklist details",
                () -> checklistRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(todayStart, todayEnd)).stream()
                .map(this::toChecklistDetail)
                .toList());

        details.put("openHazardCycles", safeList("open hazard cycle details",
                () -> hazardReportRepository.findByStatusInOrderByReportedAtDesc(OPEN_HAZARD_STATUSES)).stream()
                .map(this::toHazardDetail)
                .toList());

        details.put("openWorkStopReports", safeList("open work-stop details",
                () -> workStopReportRepository.findByStatusNotInOrderByCreatedAtDesc(CLOSED_WORK_STOP_STATUSES)).stream()
                .map(this::toWorkStopDetail)
                .toList());

        details.put("pendingRewardRequests", safeList("pending reward details",
                () -> userRewardRepository.findWithRewardByStatusOrderByCreatedAtAsc(RewardStatus.PENDING)).stream()
                .map(this::toRewardDetail)
                .toList());

        return details;
    }

    private AdminDashboardSummaryResponse.MetricDetail toUserDetail(Object[] row) {
        String teamName = stringAt(row, 6);

        return AdminDashboardSummaryResponse.MetricDetail.builder()
                .id(stringAt(row, 0))
                .title(emptyToFallback(stringAt(row, 2), stringAt(row, 1)))
                .detail(joinDetail(stringAt(row, 1), emptyToFallback(teamName, "팀 미지정")))
                .meta(roleLabel(stringAt(row, 3)))
                .status(booleanAt(row, 4) ? "본인인증 완료" : "본인인증 대기")
                .occurredAt(localDateTimeAt(row, 5))
                .build();
    }

    private AdminDashboardSummaryResponse.MetricDetail toEducationDetail(EducationCompletion completion) {
        return AdminDashboardSummaryResponse.MetricDetail.builder()
                .id(String.valueOf(completion.getId()))
                .title(completion.getUser().getName())
                .detail(completion.getEducationTitle())
                .meta(completion.getQuizScore() + "점")
                .status(completion.isQuizPassed() ? "합격" : "미합격")
                .occurredAt(completion.getCompletedAt())
                .build();
    }

    private AdminDashboardSummaryResponse.MetricDetail toChecklistDetail(Checklist checklist) {
        return AdminDashboardSummaryResponse.MetricDetail.builder()
                .id(String.valueOf(checklist.getId()))
                .title(checklist.getSiteName())
                .detail(checklist.getCreatedBy().getName())
                .meta(checklist.getWorkDate() == null ? "" : String.valueOf(checklist.getWorkDate()))
                .status(nameOf(checklist.getStatus()))
                .href("/risk-solution")
                .occurredAt(checklist.getCreatedAt())
                .build();
    }

    private AdminDashboardSummaryResponse.MetricDetail toHazardDetail(HazardReport report) {
        return AdminDashboardSummaryResponse.MetricDetail.builder()
                .id(String.valueOf(report.getId()))
                .title(emptyToFallback(report.getLocationDescription(), "위험 위치 미입력"))
                .detail(emptyToFallback(report.getHazardDescription(), "상세 설명 없음"))
                .meta(report.getReporter().getName())
                .status(nameOf(report.getStatus()))
                .href("/hazard-cycle")
                .occurredAt(report.getReportedAt())
                .build();
    }

    private AdminDashboardSummaryResponse.MetricDetail toWorkStopDetail(WorkStopReport report) {
        return AdminDashboardSummaryResponse.MetricDetail.builder()
                .id(String.valueOf(report.getId()))
                .title(emptyToFallback(report.getZone(), "작업 구역 미입력"))
                .detail(labelOf(report.getHazardType()))
                .meta(report.getIsAnonymous() ? "익명 신고" : report.getReporter().getName())
                .status(labelOf(report.getStatus()))
                .href("/work-stop-history")
                .occurredAt(report.getCreatedAt())
                .build();
    }

    private AdminDashboardSummaryResponse.MetricDetail toRewardDetail(UserReward reward) {
        return AdminDashboardSummaryResponse.MetricDetail.builder()
                .id(String.valueOf(reward.getId()))
                .title(reward.getUser().getName())
                .detail(reward.getReward().getName())
                .meta(reward.getGoldPaid() + "골드")
                .status(nameOf(reward.getStatus()))
                .href("/admin/reward-approval")
                .occurredAt(reward.getCreatedAt())
                .build();
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

    private String roleLabel(Role role) {
        return switch (role) {
            case ROLE_WORKER -> "기술인";
            case ROLE_SUPERVISOR -> "관리감독자";
            case ROLE_SAFETY_MANAGER -> "안전관리자";
            case ROLE_ADMIN -> "관리자";
            case ROLE_PROJECT_ADMIN -> "프로젝트 관리자";
        };
    }

    private String roleLabel(String role) {
        if (role == null || role.isBlank()) {
            return "";
        }

        return switch (role) {
            case "ROLE_WORKER", "WORKER", "technician" -> "기술인";
            case "ROLE_SUPERVISOR", "SUPERVISOR", "supervisor" -> "관리감독자";
            case "ROLE_SAFETY_MANAGER", "SAFETY_MANAGER", "safetyManager", "safety_manager" -> "안전관리자";
            case "ROLE_ADMIN", "ADMIN" -> "관리자";
            case "ROLE_PROJECT_ADMIN", "PROJECT_ADMIN" -> "프로젝트 관리자";
            default -> role;
        };
    }

    private String emptyToFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String stringAt(Object[] row, int index) {
        if (row == null || row.length <= index || row[index] == null) {
            return "";
        }
        return String.valueOf(row[index]);
    }

    private boolean booleanAt(Object[] row, int index) {
        if (row == null || row.length <= index || row[index] == null) {
            return false;
        }
        Object value = row[index];
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        if (value instanceof Number numberValue) {
            return numberValue.intValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private LocalDateTime localDateTimeAt(Object[] row, int index) {
        if (row == null || row.length <= index || row[index] == null) {
            return null;
        }
        Object value = row[index];
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return null;
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
                .metricDetails(Collections.emptyMap())
                .actionItems(Collections.emptyList())
                .recentActivities(Collections.emptyList())
                .workStopByHazardType(Collections.emptyMap())
                .build();
    }
}

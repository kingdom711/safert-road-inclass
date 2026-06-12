package com.jinsung.safety_road_inclass.domain.admin.service;

import com.jinsung.safety_road_inclass.domain.admin.dto.ParticipantEngagementResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminParticipantEngagementService {

    private static final List<Role> NON_PARTICIPANT_ROLES =
            List.of(Role.ROLE_ADMIN, Role.ROLE_PROJECT_ADMIN);

    private final UserRepository userRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EducationCompletionRepository educationCompletionRepository;
    private final QuestProgressRepository questProgressRepository;
    private final HazardReportRepository hazardReportRepository;
    private final HazardReportAckRepository hazardReportAckRepository;
    private final WorkStopReportRepository workStopReportRepository;
    private final PointTransactionRepository pointTransactionRepository;

    @Transactional(readOnly = true)
    public ParticipantEngagementResponse getParticipantEngagement(
            LocalDate from,
            LocalDate to,
            Long teamId,
            String keyword,
            String sort
    ) {
        LocalDate effectiveTo = Optional.ofNullable(to).orElse(LocalDate.now());
        LocalDate effectiveFrom = Optional.ofNullable(from).orElse(effectiveTo.minusDays(29));
        if (effectiveFrom.isAfter(effectiveTo)) {
            LocalDate swap = effectiveFrom;
            effectiveFrom = effectiveTo;
            effectiveTo = swap;
        }

        LocalDateTime start = effectiveFrom.atStartOfDay();
        LocalDateTime endExclusive = effectiveTo.plusDays(1).atStartOfDay();

        Map<Long, AttendanceStats> attendance = attendanceStats(effectiveFrom, effectiveTo);
        Map<Long, EducationStats> education = educationStats(start, endExclusive);
        Map<Long, QuestStats> quests = questStats(start, endExclusive);
        Map<Long, CountStats> hazards = countStats(hazardReportRepository.aggregateByReporterAndReportedAtBetween(start, endExclusive));
        Map<Long, CountStats> hazardAcks = countStats(hazardReportAckRepository.aggregateByAckerAndAckedAtBetween(start, endExclusive));
        Map<Long, CountStats> workStops = countStats(workStopReportRepository.aggregateByReporterAndCreatedAtBetween(start, endExclusive));
        Map<Long, PointStats> points = pointStats(start, endExclusive);

        String normalizedKeyword = normalize(keyword);
        List<ParticipantEngagementResponse.Row> rows = userRepository.findParticipants(NON_PARTICIPANT_ROLES).stream()
                .filter(user -> teamId == null || (user.getTeam() != null && Objects.equals(user.getTeam().getId(), teamId)))
                .filter(user -> normalizedKeyword.isBlank() || matchesKeyword(user, normalizedKeyword))
                .map(user -> buildRow(user, attendance, education, quests, hazards, hazardAcks, workStops, points))
                .sorted(comparator(sort))
                .toList();

        long activeCount = rows.stream().filter(row -> row.getLastActivityAt() != null).count();
        long totalEducation = rows.stream().mapToLong(ParticipantEngagementResponse.Row::getEducationCompletions).sum();
        long totalHazards = rows.stream().mapToLong(ParticipantEngagementResponse.Row::getHazardReports).sum();
        long totalWorkStops = rows.stream().mapToLong(ParticipantEngagementResponse.Row::getWorkStopReports).sum();
        long totalPoints = rows.stream().mapToLong(ParticipantEngagementResponse.Row::getPointsEarned).sum();
        double averageScore = rows.stream()
                .mapToInt(ParticipantEngagementResponse.Row::getEngagementScore)
                .average()
                .orElse(0);

        return ParticipantEngagementResponse.builder()
                .from(effectiveFrom)
                .to(effectiveTo)
                .summary(ParticipantEngagementResponse.Summary.builder()
                        .totalParticipants(rows.size())
                        .activeParticipants((int) activeCount)
                        .inactiveParticipants(rows.size() - (int) activeCount)
                        .averageEngagementScore(roundOneDecimal(averageScore))
                        .totalEducationCompletions(totalEducation)
                        .totalHazardReports(totalHazards)
                        .totalWorkStopReports(totalWorkStops)
                        .totalPointsEarned(totalPoints)
                        .build())
                .participants(rows)
                .build();
    }

    private ParticipantEngagementResponse.Row buildRow(
            User user,
            Map<Long, AttendanceStats> attendance,
            Map<Long, EducationStats> education,
            Map<Long, QuestStats> quests,
            Map<Long, CountStats> hazards,
            Map<Long, CountStats> hazardAcks,
            Map<Long, CountStats> workStops,
            Map<Long, PointStats> points
    ) {
        Long userId = user.getId();
        AttendanceStats attendanceStats = attendance.getOrDefault(userId, AttendanceStats.empty());
        EducationStats educationStats = education.getOrDefault(userId, EducationStats.empty());
        QuestStats questStats = quests.getOrDefault(userId, QuestStats.empty());
        CountStats hazardStats = hazards.getOrDefault(userId, CountStats.empty());
        CountStats ackStats = hazardAcks.getOrDefault(userId, CountStats.empty());
        CountStats workStopStats = workStops.getOrDefault(userId, CountStats.empty());
        PointStats pointStats = points.getOrDefault(userId, PointStats.empty());

        LocalDateTime lastActivityAt = latest(
                attendanceStats.lastCheckInDate() == null ? null : attendanceStats.lastCheckInDate().atTime(LocalTime.NOON),
                educationStats.lastActivityAt(),
                questStats.lastActivityAt(),
                hazardStats.lastActivityAt(),
                ackStats.lastActivityAt(),
                workStopStats.lastActivityAt(),
                pointStats.lastActivityAt()
        );

        return ParticipantEngagementResponse.Row.builder()
                .userId(userId)
                .username(user.getUsername())
                .name(user.getName())
                .role(roleLabel(user.getRole()))
                .teamId(user.getTeam() == null ? null : user.getTeam().getId())
                .teamName(user.getTeam() == null ? "미배정" : user.getTeam().getName())
                .attendanceCount(attendanceStats.count())
                .educationCompletions(educationStats.count())
                .averageQuizScore(educationStats.averageQuizScore())
                .questCompletions(questStats.completedCount())
                .questProgressCount(questStats.progressCount())
                .hazardReports(hazardStats.count())
                .hazardAcks(ackStats.count())
                .workStopReports(workStopStats.count())
                .pointsEarned(pointStats.amount())
                .lastActivityAt(lastActivityAt)
                .engagementScore(score(attendanceStats.count(), educationStats.count(), questStats.completedCount(),
                        hazardStats.count(), ackStats.count(), workStopStats.count()))
                .build();
    }

    private Map<Long, AttendanceStats> attendanceStats(LocalDate from, LocalDate to) {
        Map<Long, AttendanceStats> result = new HashMap<>();
        for (Object[] row : attendanceRecordRepository.aggregateByUserAndCheckInDateBetween(from, to)) {
            result.put(longAt(row, 0), new AttendanceStats(longAt(row, 1), (LocalDate) row[2]));
        }
        return result;
    }

    private Map<Long, EducationStats> educationStats(LocalDateTime start, LocalDateTime end) {
        Map<Long, EducationStats> result = new HashMap<>();
        for (Object[] row : educationCompletionRepository.aggregateByUserAndCompletedAtBetween(start, end)) {
            result.put(longAt(row, 0), new EducationStats(longAt(row, 1), doubleAt(row, 2), (LocalDateTime) row[3]));
        }
        return result;
    }

    private Map<Long, QuestStats> questStats(LocalDateTime start, LocalDateTime end) {
        Map<Long, QuestStats> result = new HashMap<>();
        for (Object[] row : questProgressRepository.aggregateByUserAndActivityBetween(start, end)) {
            result.put(longAt(row, 0), new QuestStats(longAt(row, 1), longAt(row, 2), (LocalDateTime) row[3]));
        }
        return result;
    }

    private Map<Long, CountStats> countStats(List<Object[]> rows) {
        Map<Long, CountStats> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put(longAt(row, 0), new CountStats(longAt(row, 1), (LocalDateTime) row[2]));
        }
        return result;
    }

    private Map<Long, PointStats> pointStats(LocalDateTime start, LocalDateTime end) {
        Map<Long, PointStats> result = new HashMap<>();
        for (Object[] row : pointTransactionRepository.aggregateAmountByUserAndTypeAndCreatedAtBetween(TransactionType.EARN, start, end)) {
            result.put(longAt(row, 0), new PointStats(longAt(row, 1), (LocalDateTime) row[2]));
        }
        return result;
    }

    private Comparator<ParticipantEngagementResponse.Row> comparator(String sort) {
        Comparator<ParticipantEngagementResponse.Row> comparator = switch (Optional.ofNullable(sort).orElse("score").toLowerCase(Locale.ROOT)) {
            case "name" -> Comparator.comparing(ParticipantEngagementResponse.Row::getName, Comparator.nullsLast(String::compareTo));
            case "attendance" -> Comparator.comparingLong(ParticipantEngagementResponse.Row::getAttendanceCount);
            case "education" -> Comparator.comparingLong(ParticipantEngagementResponse.Row::getEducationCompletions);
            case "hazard" -> Comparator.comparingLong(ParticipantEngagementResponse.Row::getHazardReports);
            case "points" -> Comparator.comparingLong(ParticipantEngagementResponse.Row::getPointsEarned);
            case "last" -> Comparator.comparing(ParticipantEngagementResponse.Row::getLastActivityAt, Comparator.nullsFirst(LocalDateTime::compareTo));
            default -> Comparator.comparingInt(ParticipantEngagementResponse.Row::getEngagementScore);
        };
        return "name".equalsIgnoreCase(sort) ? comparator : comparator.reversed().thenComparing(ParticipantEngagementResponse.Row::getName);
    }

    private boolean matchesKeyword(User user, String normalizedKeyword) {
        return normalize(user.getName()).contains(normalizedKeyword)
                || normalize(user.getUsername()).contains(normalizedKeyword)
                || normalize(user.getEmail()).contains(normalizedKeyword)
                || (user.getTeam() != null && normalize(user.getTeam().getName()).contains(normalizedKeyword));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String roleLabel(Role role) {
        if (role == null) {
            return "";
        }
        return switch (role) {
            case ROLE_WORKER -> "근로자";
            case ROLE_SUPERVISOR -> "관리감독자";
            case ROLE_SAFETY_MANAGER -> "안전관리자";
            case ROLE_ADMIN -> "관리자";
            case ROLE_PROJECT_ADMIN -> "프로젝트 관리자";
        };
    }

    private int score(long attendance, long education, long quests, long hazards, long acks, long workStops) {
        long value = attendance * 2 + education * 8 + quests * 5 + hazards * 12 + acks * 4 + workStops * 15;
        return (int) Math.min(100, Math.max(0, value));
    }

    private LocalDateTime latest(LocalDateTime... values) {
        LocalDateTime latest = null;
        for (LocalDateTime value : values) {
            if (value != null && (latest == null || value.isAfter(latest))) {
                latest = value;
            }
        }
        return latest;
    }

    private long longAt(Object[] row, int index) {
        Object value = row[index];
        return value instanceof Number number ? number.longValue() : 0;
    }

    private double doubleAt(Object[] row, int index) {
        Object value = row[index];
        return value instanceof Number number ? roundOneDecimal(number.doubleValue()) : 0;
    }

    private double roundOneDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private record AttendanceStats(long count, LocalDate lastCheckInDate) {
        static AttendanceStats empty() {
            return new AttendanceStats(0, null);
        }
    }

    private record EducationStats(long count, double averageQuizScore, LocalDateTime lastActivityAt) {
        static EducationStats empty() {
            return new EducationStats(0, 0, null);
        }
    }

    private record QuestStats(long completedCount, long progressCount, LocalDateTime lastActivityAt) {
        static QuestStats empty() {
            return new QuestStats(0, 0, null);
        }
    }

    private record CountStats(long count, LocalDateTime lastActivityAt) {
        static CountStats empty() {
            return new CountStats(0, null);
        }
    }

    private record PointStats(long amount, LocalDateTime lastActivityAt) {
        static PointStats empty() {
            return new PointStats(0, null);
        }
    }
}

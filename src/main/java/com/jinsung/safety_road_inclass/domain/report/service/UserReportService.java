package com.jinsung.safety_road_inclass.domain.report.service;

import com.jinsung.safety_road_inclass.domain.attendance.repository.AttendanceRecordRepository;
import com.jinsung.safety_road_inclass.domain.education.repository.EducationCompletionRepository;
import com.jinsung.safety_road_inclass.domain.hazardcycle.repository.HazardReportRepository;
import com.jinsung.safety_road_inclass.domain.point.entity.TransactionType;
import com.jinsung.safety_road_inclass.domain.point.repository.PointTransactionRepository;
import com.jinsung.safety_road_inclass.domain.quest.repository.QuestProgressRepository;
import com.jinsung.safety_road_inclass.domain.report.dto.ReportCategoryScore;
import com.jinsung.safety_road_inclass.domain.report.dto.ReportSummaryResponse;
import com.jinsung.safety_road_inclass.domain.report.dto.ReportTrendItem;
import com.jinsung.safety_road_inclass.domain.report.dto.UserReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReportService {

    private static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("M월 d일");

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final QuestProgressRepository questProgressRepository;
    private final EducationCompletionRepository educationCompletionRepository;
    private final HazardReportRepository hazardReportRepository;

    public UserReportResponse generate(Long userId, String typeValue) {
        ReportPeriodType type = ReportPeriodType.from(typeValue);
        LocalDate today = LocalDate.now();
        PeriodRange current = resolveCurrentPeriod(type, today);
        PeriodRange previous = current.previous();

        PeriodStats currentStats = collectStats(userId, current);
        PeriodStats previousStats = collectStats(userId, previous);
        List<ReportCategoryScore> categoryScores = buildCategoryScores(currentStats, current);
        int overallScore = averageScore(categoryScores);
        int previousOverallScore = averageScore(buildCategoryScores(previousStats, previous));

        return UserReportResponse.builder()
                .periodType(type.name().toLowerCase())
                .periodLabel(buildPeriodLabel(type, current))
                .periodStart(current.start())
                .periodEnd(current.endInclusive())
                .summary(buildSummary(currentStats, overallScore, overallScore - previousOverallScore))
                .categoryScores(categoryScores)
                .trends(buildTrends(userId, type, current))
                .strengths(buildStrengths(currentStats, previousStats, categoryScores))
                .improvements(buildImprovements(currentStats, previousStats, categoryScores))
                .recommendations(buildRecommendations(currentStats, categoryScores, current))
                .build();
    }

    private PeriodStats collectStats(Long userId, PeriodRange range) {
        LocalDateTime start = range.start().atStartOfDay();
        LocalDateTime end = range.endExclusive().atStartOfDay();

        long loginDays = attendanceRecordRepository.countByUserIdAndCheckInDateBetween(
                userId,
                range.start(),
                range.endInclusive()
        );
        long completedQuests = questProgressRepository.countCompletedByUserIdAndCompletedAtBetween(userId, start, end);
        long educationCompletions = educationCompletionRepository
                .countByUserIdAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(userId, start, end);
        long passedEducations = educationCompletionRepository
                .countByUserIdAndCompletedAtGreaterThanEqualAndCompletedAtLessThanAndQuizPassed(userId, start, end, true);
        int averageQuizScore = (int) Math.round(
                educationCompletionRepository.averageQuizScoreByUserIdAndCompletedAtBetween(userId, start, end)
        );
        long hazardReports = hazardReportRepository
                .countByReporterIdAndReportedAtGreaterThanEqualAndReportedAtLessThan(userId, start, end);
        long hazardCompletions = hazardReportRepository
                .countByReporterIdAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(userId, start, end);
        int earnedPoints = pointTransactionRepository
                .sumAmountByUserIdAndTypeAndCreatedAtBetween(userId, TransactionType.EARN, start, end);
        int spentPoints = pointTransactionRepository
                .sumAmountByUserIdAndTypeAndCreatedAtBetween(userId, TransactionType.SPEND, start, end);

        return new PeriodStats(
                completedQuests,
                educationCompletions,
                passedEducations,
                averageQuizScore,
                loginDays,
                hazardReports,
                hazardCompletions,
                earnedPoints,
                spentPoints
        );
    }

    private ReportSummaryResponse buildSummary(PeriodStats stats, int overallScore, int scoreChange) {
        return ReportSummaryResponse.builder()
                .completedQuests(stats.completedQuests())
                .educationCompletions(stats.educationCompletions())
                .passedEducations(stats.passedEducations())
                .averageQuizScore(stats.averageQuizScore())
                .loginDays(stats.loginDays())
                .hazardReports(stats.hazardReports())
                .hazardCompletions(stats.hazardCompletions())
                .earnedPoints(stats.earnedPoints())
                .spentPoints(stats.spentPoints())
                .overallScore(overallScore)
                .scoreChange(scoreChange)
                .build();
    }

    private List<ReportCategoryScore> buildCategoryScores(PeriodStats stats, PeriodRange range) {
        int days = Math.max(1, (int) ChronoUnit.DAYS.between(range.start(), range.endExclusive()));
        int attendanceScore = clamp((int) Math.round((stats.loginDays() * 100.0) / days));
        int questTarget = Math.max(1, days);
        int questScore = clamp((int) Math.round((stats.completedQuests() * 100.0) / questTarget));
        int educationScore = stats.educationCompletions() == 0 ? 0 : clamp(stats.averageQuizScore());
        int hazardScore = buildHazardScore(stats);
        int pointTarget = Math.max(100, days * 40);
        int pointScore = clamp((int) Math.round((stats.earnedPoints() * 100.0) / pointTarget));

        return List.of(
                category("attendance", "참여도", attendanceScore, stats.loginDays() + "일 접속"),
                category("quest", "퀘스트 수행", questScore, stats.completedQuests() + "개 완료"),
                category("education", "교육 성과", educationScore, stats.averageQuizScore() + "점 평균"),
                category("hazard", "위험 개선", hazardScore, stats.hazardReports() + "건 신고 / " + stats.hazardCompletions() + "건 조치"),
                category("points", "보상 활동", pointScore, stats.earnedPoints() + "P 적립")
        );
    }

    private int buildHazardScore(PeriodStats stats) {
        if (stats.hazardReports() == 0 && stats.hazardCompletions() == 0) {
            return 50;
        }

        double completionRate = stats.hazardReports() == 0
                ? 1
                : Math.min(1.0, stats.hazardCompletions() / (double) stats.hazardReports());
        double reportActivity = Math.min(1.0, stats.hazardReports() / 3.0);
        return clamp((int) Math.round((completionRate * 70) + (reportActivity * 30)));
    }

    private ReportCategoryScore category(String key, String label, int score, String detail) {
        return ReportCategoryScore.builder()
                .key(key)
                .label(label)
                .score(clamp(score))
                .detail(detail)
                .build();
    }

    private int averageScore(List<ReportCategoryScore> scores) {
        return (int) Math.round(scores.stream()
                .mapToInt(ReportCategoryScore::getScore)
                .average()
                .orElse(0));
    }

    private List<ReportTrendItem> buildTrends(Long userId, ReportPeriodType type, PeriodRange current) {
        List<PeriodRange> buckets = switch (type) {
            case WEEKLY -> dailyBuckets(current);
            case MONTHLY -> weeklyBuckets(current);
            case YEARLY -> monthlyBuckets(current);
        };

        return buckets.stream()
                .map(range -> toTrendItem(userId, range))
                .toList();
    }

    private ReportTrendItem toTrendItem(Long userId, PeriodRange range) {
        PeriodStats stats = collectStats(userId, range);
        int activityScore = clamp((int) (stats.completedQuests()
                + stats.educationCompletions()
                + stats.loginDays()
                + stats.hazardReports()
                + stats.hazardCompletions()
                + Math.round(stats.earnedPoints() / 50.0)));

        return ReportTrendItem.builder()
                .label(buildBucketLabel(range))
                .startDate(range.start())
                .endDate(range.endInclusive())
                .completedQuests(stats.completedQuests())
                .educationCompletions(stats.educationCompletions())
                .loginDays(stats.loginDays())
                .hazardReports(stats.hazardReports())
                .hazardCompletions(stats.hazardCompletions())
                .earnedPoints(stats.earnedPoints())
                .activityScore(activityScore)
                .build();
    }

    private List<String> buildStrengths(PeriodStats current, PeriodStats previous, List<ReportCategoryScore> scores) {
        List<String> strengths = new ArrayList<>();

        scores.stream()
                .filter(score -> score.getScore() >= 80)
                .limit(2)
                .forEach(score -> strengths.add(score.getLabel() + " 영역이 " + score.getScore() + "점으로 우수합니다."));

        if (current.completedQuests() > previous.completedQuests()) {
            strengths.add("지난 기간보다 퀘스트 완료 수가 증가했습니다.");
        }
        if (current.earnedPoints() > previous.earnedPoints()) {
            strengths.add("포인트 적립량이 늘어 활동 보상 흐름이 좋아졌습니다.");
        }
        if (current.hazardCompletions() > 0) {
            strengths.add("위험요인 조치 완료 이력이 있어 현장 개선 활동이 확인됩니다.");
        }

        if (strengths.isEmpty()) {
            strengths.add("이번 기간의 활동 데이터가 쌓이기 시작했습니다.");
        }
        return strengths.stream().limit(3).toList();
    }

    private List<String> buildImprovements(PeriodStats current, PeriodStats previous, List<ReportCategoryScore> scores) {
        List<String> improvements = new ArrayList<>();

        scores.stream()
                .filter(score -> score.getScore() < 60)
                .limit(2)
                .forEach(score -> improvements.add(score.getLabel() + " 영역 보강이 필요합니다. 현재 " + score.getDetail() + "입니다."));

        if (current.loginDays() < previous.loginDays()) {
            improvements.add("지난 기간보다 접속일이 줄었습니다.");
        }
        if (current.educationCompletions() > 0 && current.averageQuizScore() < 70) {
            improvements.add("교육 퀴즈 평균 점수가 낮아 복습이 필요합니다.");
        }
        if (current.hazardReports() > current.hazardCompletions()) {
            improvements.add("신고된 위험요인 중 조치 완료되지 않은 항목이 남아 있습니다.");
        }

        if (improvements.isEmpty()) {
            improvements.add("큰 취약점은 없지만 활동을 꾸준히 유지하는 것이 중요합니다.");
        }
        return improvements.stream().limit(3).toList();
    }

    private List<String> buildRecommendations(PeriodStats stats, List<ReportCategoryScore> scores, PeriodRange range) {
        List<String> recommendations = new ArrayList<>();
        int days = Math.max(1, (int) ChronoUnit.DAYS.between(range.start(), range.endExclusive()));

        for (ReportCategoryScore score : scores) {
            if (score.getScore() >= 60) {
                continue;
            }

            switch (score.getKey()) {
                case "attendance" -> recommendations.add("다음 기간에는 최소 " + Math.min(days, 5) + "일 이상 접속을 목표로 잡으세요.");
                case "quest" -> recommendations.add("미완료 퀘스트를 우선 처리하고 하루 1개 완료 루틴을 만드세요.");
                case "education" -> recommendations.add("안전교육을 1개 이상 수료하고 퀴즈 오답을 복습하세요.");
                case "hazard" -> recommendations.add("현장 위험요인 1건 이상 신고하고 조치 완료까지 연결하세요.");
                case "points" -> recommendations.add("출석, 교육, 퀘스트 보상으로 포인트 적립 흐름을 회복하세요.");
                default -> {
                }
            }
        }

        if (stats.hazardReports() > stats.hazardCompletions()) {
            recommendations.add("이미 신고한 위험요인은 완료 사진과 조치 내용을 등록해 마무리하세요.");
        }
        if (recommendations.isEmpty()) {
            recommendations.add("현재 페이스를 유지하면서 교육 1건과 위험요인 개선 1건을 추가 목표로 잡으세요.");
        }
        return recommendations.stream().limit(3).toList();
    }

    private PeriodRange resolveCurrentPeriod(ReportPeriodType type, LocalDate today) {
        LocalDate tomorrow = today.plusDays(1);
        return switch (type) {
            case WEEKLY -> {
                LocalDate start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate end = min(start.plusDays(7), tomorrow);
                yield new PeriodRange(start, end);
            }
            case MONTHLY -> {
                YearMonth month = YearMonth.from(today);
                LocalDate end = min(month.plusMonths(1).atDay(1), tomorrow);
                yield new PeriodRange(month.atDay(1), end);
            }
            case YEARLY -> {
                LocalDate start = LocalDate.of(today.getYear(), 1, 1);
                LocalDate end = min(LocalDate.of(today.getYear() + 1, 1, 1), tomorrow);
                yield new PeriodRange(start, end);
            }
        };
    }

    private String buildPeriodLabel(ReportPeriodType type, PeriodRange range) {
        return switch (type) {
            case WEEKLY -> range.start().getYear() + "년 " + range.start().format(MONTH_LABEL)
                    + " - " + range.endInclusive().format(MONTH_LABEL);
            case MONTHLY -> range.start().getYear() + "년 " + range.start().getMonthValue() + "월";
            case YEARLY -> range.start().getYear() + "년";
        };
    }

    private String buildBucketLabel(PeriodRange range) {
        if (range.start().equals(range.endInclusive())) {
            return range.start().format(DateTimeFormatter.ofPattern("M/d"));
        }
        return range.start().format(MONTH_LABEL) + " - " + range.endInclusive().format(MONTH_LABEL);
    }

    private List<PeriodRange> dailyBuckets(PeriodRange range) {
        List<PeriodRange> buckets = new ArrayList<>();
        LocalDate cursor = range.start();
        while (cursor.isBefore(range.endExclusive())) {
            buckets.add(new PeriodRange(cursor, cursor.plusDays(1)));
            cursor = cursor.plusDays(1);
        }
        return buckets;
    }

    private List<PeriodRange> weeklyBuckets(PeriodRange range) {
        List<PeriodRange> buckets = new ArrayList<>();
        LocalDate cursor = range.start();
        while (cursor.isBefore(range.endExclusive())) {
            LocalDate next = cursor.plusDays(7);
            if (next.isAfter(range.endExclusive())) {
                next = range.endExclusive();
            }
            buckets.add(new PeriodRange(cursor, next));
            cursor = next;
        }
        return buckets;
    }

    private List<PeriodRange> monthlyBuckets(PeriodRange range) {
        List<PeriodRange> buckets = new ArrayList<>();
        LocalDate cursor = range.start();
        while (cursor.isBefore(range.endExclusive())) {
            YearMonth month = YearMonth.from(cursor);
            buckets.add(new PeriodRange(month.atDay(1), month.plusMonths(1).atDay(1)));
            cursor = month.plusMonths(1).atDay(1);
        }
        return buckets;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private LocalDate min(LocalDate first, LocalDate second) {
        return first.isBefore(second) ? first : second;
    }

    private record PeriodRange(LocalDate start, LocalDate endExclusive) {
        LocalDate endInclusive() {
            return endExclusive.minusDays(1);
        }

        PeriodRange previous() {
            long days = ChronoUnit.DAYS.between(start, endExclusive);
            return new PeriodRange(start.minusDays(days), start);
        }
    }

    private record PeriodStats(
            long completedQuests,
            long educationCompletions,
            long passedEducations,
            int averageQuizScore,
            long loginDays,
            long hazardReports,
            long hazardCompletions,
            int earnedPoints,
            int spentPoints
    ) {
    }
}

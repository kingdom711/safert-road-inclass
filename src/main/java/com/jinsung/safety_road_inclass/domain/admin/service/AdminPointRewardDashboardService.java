package com.jinsung.safety_road_inclass.domain.admin.service;

import com.jinsung.safety_road_inclass.domain.admin.dto.PointRewardDashboardResponse;
import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.gold.entity.GoldTransactionType;
import com.jinsung.safety_road_inclass.domain.gold.entity.UserGold;
import com.jinsung.safety_road_inclass.domain.gold.repository.GoldTransactionRepository;
import com.jinsung.safety_road_inclass.domain.gold.repository.UserGoldRepository;
import com.jinsung.safety_road_inclass.domain.point.entity.TransactionType;
import com.jinsung.safety_road_inclass.domain.point.entity.UserPoints;
import com.jinsung.safety_road_inclass.domain.point.repository.PointTransactionRepository;
import com.jinsung.safety_road_inclass.domain.point.repository.UserPointsRepository;
import com.jinsung.safety_road_inclass.domain.reward.entity.RewardStatus;
import com.jinsung.safety_road_inclass.domain.reward.repository.UserRewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class AdminPointRewardDashboardService {

    @SuppressWarnings("unused")
    private static final List<Role> NON_PARTICIPANT_ROLES =
            List.of(Role.ROLE_ADMIN, Role.ROLE_PROJECT_ADMIN);

    private final UserRepository userRepository;
    private final UserPointsRepository userPointsRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final UserGoldRepository userGoldRepository;
    private final GoldTransactionRepository goldTransactionRepository;
    private final UserRewardRepository userRewardRepository;

    @Transactional(readOnly = true)
    public PointRewardDashboardResponse getPointRewardDashboard(
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

        Map<Long, UserPoints> pointBalances = pointBalances();
        Map<Long, AmountStats> pointEarned = amountStats(safeList("point earned stats",
                () -> pointTransactionRepository.aggregateAmountByUserAndTypeAndCreatedAtBetween(TransactionType.EARN, start, endExclusive)));
        Map<Long, AmountStats> pointSpent = amountStats(safeList("point spent stats",
                () -> pointTransactionRepository.aggregateAmountByUserAndTypeAndCreatedAtBetween(TransactionType.SPEND, start, endExclusive)));
        Map<Long, UserGold> goldBalances = goldBalances();
        Map<Long, AmountStats> goldEarned = amountStats(safeList("gold earned stats",
                () -> goldTransactionRepository.aggregateAmountByUserAndTypeAndCreatedAtBetween(GoldTransactionType.EARN, start, endExclusive)));
        Map<Long, AmountStats> goldSpent = amountStats(safeList("gold spent stats",
                () -> goldTransactionRepository.aggregateAmountByUserAndTypeAndCreatedAtBetween(GoldTransactionType.SPEND, start, endExclusive)));
        Map<Long, RewardStats> pendingRewards = rewardStats(safeList("pending reward stats",
                () -> userRewardRepository.aggregateByUserAndStatus(RewardStatus.PENDING)));

        String normalizedKeyword = normalize(keyword);
        List<PointRewardDashboardResponse.UserBalanceRow> rows = participants().stream()
                .filter(user -> teamId == null || Objects.equals(user.teamId(), teamId))
                .filter(user -> normalizedKeyword.isBlank() || matchesKeyword(user, normalizedKeyword))
                .map(user -> buildUserRow(user, pointBalances, pointEarned, pointSpent, goldBalances, goldEarned, goldSpent, pendingRewards))
                .sorted(comparator(sort))
                .toList();

        List<PointRewardDashboardResponse.RewardDemandRow> demandRows = safeList("reward demand stats",
                () -> userRewardRepository.aggregateRewardDemandByStatus(RewardStatus.PENDING))
                .stream()
                .map(this::toRewardDemandRow)
                .toList();

        return PointRewardDashboardResponse.builder()
                .from(effectiveFrom)
                .to(effectiveTo)
                .summary(buildSummary(rows, demandRows))
                .users(rows)
                .pendingRewards(demandRows)
                .build();
    }

    private PointRewardDashboardResponse.UserBalanceRow buildUserRow(
            ParticipantUser user,
            Map<Long, UserPoints> pointBalances,
            Map<Long, AmountStats> pointEarned,
            Map<Long, AmountStats> pointSpent,
            Map<Long, UserGold> goldBalances,
            Map<Long, AmountStats> goldEarned,
            Map<Long, AmountStats> goldSpent,
            Map<Long, RewardStats> pendingRewards
    ) {
        Long userId = user.id();
        UserPoints points = pointBalances.get(userId);
        UserGold gold = goldBalances.get(userId);
        AmountStats periodPointEarned = pointEarned.getOrDefault(userId, AmountStats.empty());
        AmountStats periodPointSpent = pointSpent.getOrDefault(userId, AmountStats.empty());
        AmountStats periodGoldEarned = goldEarned.getOrDefault(userId, AmountStats.empty());
        AmountStats periodGoldSpent = goldSpent.getOrDefault(userId, AmountStats.empty());
        RewardStats pending = pendingRewards.getOrDefault(userId, RewardStats.empty());

        return PointRewardDashboardResponse.UserBalanceRow.builder()
                .userId(userId)
                .username(user.username())
                .name(user.name())
                .teamId(user.teamId())
                .teamName(emptyToFallback(user.teamName(), "팀 미지정"))
                .pointBalance(points == null ? 0 : points.getBalance())
                .totalPointsEarned(points == null ? 0 : points.getTotalEarned())
                .totalPointsSpent(points == null ? 0 : points.getTotalSpent())
                .periodPointsEarned(periodPointEarned.amount())
                .periodPointsSpent(periodPointSpent.amount())
                .goldBalance(gold == null ? 0 : gold.getBalance())
                .totalGoldEarned(gold == null ? 0 : gold.getTotalEarned())
                .totalGoldSpent(gold == null ? 0 : gold.getTotalSpent())
                .periodGoldEarned(periodGoldEarned.amount())
                .periodGoldSpent(periodGoldSpent.amount())
                .pendingRewardRequests(pending.count())
                .pendingRewardGold(pending.amount())
                .lastPointActivityAt(latest(periodPointEarned.lastActivityAt(), periodPointSpent.lastActivityAt(),
                        periodGoldEarned.lastActivityAt(), periodGoldSpent.lastActivityAt(), pending.lastActivityAt()))
                .build();
    }

    private PointRewardDashboardResponse.Summary buildSummary(
            List<PointRewardDashboardResponse.UserBalanceRow> rows,
            List<PointRewardDashboardResponse.RewardDemandRow> demandRows
    ) {
        return PointRewardDashboardResponse.Summary.builder()
                .totalParticipants(rows.size())
                .totalPointBalance(rows.stream().mapToLong(PointRewardDashboardResponse.UserBalanceRow::getPointBalance).sum())
                .periodPointsEarned(rows.stream().mapToLong(PointRewardDashboardResponse.UserBalanceRow::getPeriodPointsEarned).sum())
                .periodPointsSpent(rows.stream().mapToLong(PointRewardDashboardResponse.UserBalanceRow::getPeriodPointsSpent).sum())
                .totalGoldBalance(rows.stream().mapToLong(PointRewardDashboardResponse.UserBalanceRow::getGoldBalance).sum())
                .periodGoldEarned(rows.stream().mapToLong(PointRewardDashboardResponse.UserBalanceRow::getPeriodGoldEarned).sum())
                .periodGoldSpent(rows.stream().mapToLong(PointRewardDashboardResponse.UserBalanceRow::getPeriodGoldSpent).sum())
                .pendingRewardRequests(demandRows.stream().mapToLong(PointRewardDashboardResponse.RewardDemandRow::getPendingCount).sum())
                .pendingRewardGold(demandRows.stream().mapToLong(PointRewardDashboardResponse.RewardDemandRow::getRequiredGold).sum())
                .pendingRewardCashValue(demandRows.stream().mapToLong(PointRewardDashboardResponse.RewardDemandRow::getCashValue).sum())
                .build();
    }

    private PointRewardDashboardResponse.RewardDemandRow toRewardDemandRow(Object[] row) {
        return PointRewardDashboardResponse.RewardDemandRow.builder()
                .rewardId(longAt(row, 0))
                .rewardName(stringAt(row, 1))
                .rewardType(stringAt(row, 2))
                .pendingCount(longAt(row, 3))
                .requiredGold(longAt(row, 4))
                .cashValue(longAt(row, 5))
                .remainingQuantity((int) longAt(row, 6))
                .build();
    }

    private List<ParticipantUser> participants() {
        return safeList("participant rows", userRepository::findParticipantAdminRows).stream()
                .map(row -> new ParticipantUser(
                        longAt(row, 0),
                        stringAt(row, 1),
                        stringAt(row, 2),
                        stringAt(row, 3),
                        nullableLongAt(row, 5),
                        stringAt(row, 6)
                ))
                .toList();
    }

    private Map<Long, UserPoints> pointBalances() {
        Map<Long, UserPoints> result = new HashMap<>();
        for (UserPoints points : safeList("point balances", userPointsRepository::findAllOrderByBalanceDesc)) {
            result.put(points.getUser().getId(), points);
        }
        return result;
    }

    private Map<Long, UserGold> goldBalances() {
        Map<Long, UserGold> result = new HashMap<>();
        for (UserGold gold : safeList("gold balances", userGoldRepository::findAllWithUser)) {
            result.put(gold.getUser().getId(), gold);
        }
        return result;
    }

    private Map<Long, AmountStats> amountStats(List<Object[]> rows) {
        Map<Long, AmountStats> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put(longAt(row, 0), new AmountStats(longAt(row, 1), (LocalDateTime) row[2]));
        }
        return result;
    }

    private Map<Long, RewardStats> rewardStats(List<Object[]> rows) {
        Map<Long, RewardStats> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put(longAt(row, 0), new RewardStats(longAt(row, 1), longAt(row, 2), (LocalDateTime) row[3]));
        }
        return result;
    }

    private Comparator<PointRewardDashboardResponse.UserBalanceRow> comparator(String sort) {
        Comparator<PointRewardDashboardResponse.UserBalanceRow> comparator = switch (Optional.ofNullable(sort).orElse("balance").toLowerCase(Locale.ROOT)) {
            case "name" -> Comparator.comparing(PointRewardDashboardResponse.UserBalanceRow::getName, Comparator.nullsLast(String::compareTo));
            case "earned" -> Comparator.comparingLong(PointRewardDashboardResponse.UserBalanceRow::getPeriodPointsEarned);
            case "spent" -> Comparator.comparingLong(PointRewardDashboardResponse.UserBalanceRow::getPeriodPointsSpent);
            case "gold" -> Comparator.comparingInt(PointRewardDashboardResponse.UserBalanceRow::getGoldBalance);
            case "pending" -> Comparator.comparingLong(PointRewardDashboardResponse.UserBalanceRow::getPendingRewardRequests);
            case "last" -> Comparator.comparing(PointRewardDashboardResponse.UserBalanceRow::getLastPointActivityAt, Comparator.nullsFirst(LocalDateTime::compareTo));
            default -> Comparator.comparingInt(PointRewardDashboardResponse.UserBalanceRow::getPointBalance);
        };
        return "name".equalsIgnoreCase(sort) ? comparator : comparator.reversed().thenComparing(PointRewardDashboardResponse.UserBalanceRow::getName);
    }

    private boolean matchesKeyword(ParticipantUser user, String normalizedKeyword) {
        return normalize(user.name()).contains(normalizedKeyword)
                || normalize(user.username()).contains(normalizedKeyword)
                || normalize(user.email()).contains(normalizedKeyword)
                || normalize(user.teamName()).contains(normalizedKeyword);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
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

    private <T> List<T> safeList(String label, Supplier<List<T>> supplier) {
        try {
            List<T> values = supplier.get();
            return values == null ? List.of() : values;
        } catch (Exception e) {
            return List.of();
        }
    }

    private long longAt(Object[] row, int index) {
        Object value = row[index];
        return value instanceof Number number ? number.longValue() : 0;
    }

    private Long nullableLongAt(Object[] row, int index) {
        Object value = row[index];
        return value instanceof Number number ? number.longValue() : null;
    }

    private String stringAt(Object[] row, int index) {
        Object value = row[index];
        return value == null ? "" : String.valueOf(value);
    }

    private String emptyToFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private record ParticipantUser(
            Long id,
            String username,
            String name,
            String email,
            Long teamId,
            String teamName
    ) {
    }

    private record AmountStats(long amount, LocalDateTime lastActivityAt) {
        static AmountStats empty() {
            return new AmountStats(0, null);
        }
    }

    private record RewardStats(long count, long amount, LocalDateTime lastActivityAt) {
        static RewardStats empty() {
            return new RewardStats(0, 0, null);
        }
    }
}

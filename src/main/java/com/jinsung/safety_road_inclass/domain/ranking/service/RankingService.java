package com.jinsung.safety_road_inclass.domain.ranking.service;

import com.jinsung.safety_road_inclass.domain.attendance.entity.UserStreak;
import com.jinsung.safety_road_inclass.domain.attendance.repository.UserStreakRepository;
import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.gameprofile.entity.UserGameProfile;
import com.jinsung.safety_road_inclass.domain.gameprofile.repository.GameProfileRepository;
import com.jinsung.safety_road_inclass.domain.point.entity.UserPoints;
import com.jinsung.safety_road_inclass.domain.point.repository.UserPointsRepository;
import com.jinsung.safety_road_inclass.domain.ranking.dto.MyRankResponse;
import com.jinsung.safety_road_inclass.domain.ranking.dto.RankingEntryResponse;
import com.jinsung.safety_road_inclass.domain.ranking.dto.TeamRankingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RankingService - 랭킹 조회 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final UserPointsRepository userPointsRepository;
    private final UserStreakRepository userStreakRepository;
    private final GameProfileRepository gameProfileRepository;
    private final UserRepository userRepository;

    private static final Map<String, String> ROLE_TEAM_NAMES = Map.of(
            "ROLE_WORKER", "기술자팀",
            "ROLE_SUPERVISOR", "관리감독자팀",
            "ROLE_SAFETY_MANAGER", "안전관리자팀",
            "ROLE_ADMIN", "관리자팀"
    );

    /**
     * 개인 랭킹 조회
     *
     * @param type   points | level | streak
     * @param limit  조회 수
     * @param role   역할 필터 (optional, e.g. "WORKER")
     */
    public List<RankingEntryResponse> getRankings(String type, int limit, String role) {
        Role roleFilter = parseRole(role);

        return switch (type) {
            case "level" -> getLevelRankings(limit, roleFilter);
            case "streak" -> getStreakRankings(limit, roleFilter);
            default -> getPointsRankings(limit, roleFilter);
        };
    }

    /**
     * 현재 사용자 랭킹 조회
     */
    public MyRankResponse getMyRank(Long userId, String type) {
        List<RankingEntryResponse> allRankings = getRankings(type, Integer.MAX_VALUE, null);
        int totalUsers = allRankings.size();

        for (RankingEntryResponse entry : allRankings) {
            if (entry.getUserId().equals(userId)) {
                return MyRankResponse.of(
                        entry.getRank(), totalUsers, userId,
                        entry.getName(), entry.getRole(),
                        entry.getPoints(), entry.getLevel(), entry.getStreak()
                );
            }
        }

        // 랭킹에 없는 경우 (데이터가 아직 없는 신규 유저)
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return MyRankResponse.of(0, totalUsers, userId, "", "", 0, 1, 0);
        }

        return MyRankResponse.of(
                totalUsers + 1, totalUsers, userId,
                user.getName(), user.getRole().getSimpleName(),
                0, 1, 0
        );
    }

    /**
     * 팀(역할 그룹) 랭킹 조회
     */
    public List<TeamRankingResponse> getTeamRankings(String type, int limit) {
        List<UserPoints> allPoints = userPointsRepository.findAllOrderByBalanceDesc();

        // Role별 그룹화
        Map<Role, List<UserPoints>> grouped = allPoints.stream()
                .collect(Collectors.groupingBy(up -> up.getUser().getRole()));

        List<TeamRankingResponse> teams = new ArrayList<>();

        for (Map.Entry<Role, List<UserPoints>> entry : grouped.entrySet()) {
            Role teamRole = entry.getKey();
            List<UserPoints> members = entry.getValue();

            long totalPoints = members.stream().mapToLong(UserPoints::getBalance).sum();
            int avgPoints = members.isEmpty() ? 0 : (int) (totalPoints / members.size());

            // 최고 포인트 멤버
            String topMember = members.stream()
                    .max(Comparator.comparingInt(UserPoints::getBalance))
                    .map(up -> up.getUser().getName())
                    .orElse("");

            teams.add(TeamRankingResponse.of(
                    0, // rank는 아래에서 정렬 후 설정
                    ROLE_TEAM_NAMES.getOrDefault(teamRole.name(), teamRole.getSimpleName()),
                    members.size(),
                    totalPoints,
                    avgPoints,
                    topMember
            ));
        }

        // 정렬 기준 결정
        switch (type) {
            case "level", "streak" ->
                    teams.sort(Comparator.comparingInt(TeamRankingResponse::getAvgPoints).reversed());
            default ->
                    teams.sort(Comparator.comparingLong(TeamRankingResponse::getTotalPoints).reversed());
        }

        // 순위 부여 및 limit 적용
        List<TeamRankingResponse> result = new ArrayList<>();
        for (int i = 0; i < Math.min(teams.size(), limit); i++) {
            TeamRankingResponse t = teams.get(i);
            result.add(TeamRankingResponse.of(
                    i + 1, t.getTeamName(), t.getMemberCount(),
                    t.getTotalPoints(), t.getAvgPoints(), t.getTopMember()
            ));
        }

        return result;
    }

    // ─── 포인트 랭킹 ───

    private List<RankingEntryResponse> getPointsRankings(int limit, Role roleFilter) {
        List<UserPoints> allPoints = userPointsRepository.findAllOrderByBalanceDesc();

        // 보조 데이터 맵 구성
        Map<Long, UserGameProfile> profileMap = buildProfileMap();
        Map<Long, UserStreak> streakMap = buildStreakMap();

        List<RankingEntryResponse> result = new ArrayList<>();
        int rank = 0;

        for (UserPoints up : allPoints) {
            User user = up.getUser();
            if (roleFilter != null && user.getRole() != roleFilter) continue;

            rank++;
            if (rank > limit) break;

            UserGameProfile profile = profileMap.get(user.getId());
            UserStreak streak = streakMap.get(user.getId());

            result.add(RankingEntryResponse.of(
                    rank, user.getId(), user.getName(),
                    user.getRole().getSimpleName(),
                    up.getBalance(),
                    profile != null ? profile.getLevel() : 1,
                    streak != null ? streak.getCurrentStreak() : 0
            ));
        }

        return result;
    }

    // ─── 레벨 랭킹 ───

    private List<RankingEntryResponse> getLevelRankings(int limit, Role roleFilter) {
        List<UserGameProfile> allProfiles = gameProfileRepository.findAllOrderByLevelDesc();

        Map<Long, UserPoints> pointsMap = buildPointsMap();
        Map<Long, UserStreak> streakMap = buildStreakMap();

        List<RankingEntryResponse> result = new ArrayList<>();
        int rank = 0;

        for (UserGameProfile gp : allProfiles) {
            User user = gp.getUser();
            if (roleFilter != null && user.getRole() != roleFilter) continue;

            rank++;
            if (rank > limit) break;

            UserPoints points = pointsMap.get(user.getId());
            UserStreak streak = streakMap.get(user.getId());

            result.add(RankingEntryResponse.of(
                    rank, user.getId(), user.getName(),
                    user.getRole().getSimpleName(),
                    points != null ? points.getBalance() : 0,
                    gp.getLevel(),
                    streak != null ? streak.getCurrentStreak() : 0
            ));
        }

        return result;
    }

    // ─── 스트릭 랭킹 ───

    private List<RankingEntryResponse> getStreakRankings(int limit, Role roleFilter) {
        List<UserStreak> allStreaks = userStreakRepository.findAllOrderByCurrentStreakDesc();

        Map<Long, UserPoints> pointsMap = buildPointsMap();
        Map<Long, UserGameProfile> profileMap = buildProfileMap();

        List<RankingEntryResponse> result = new ArrayList<>();
        int rank = 0;

        for (UserStreak us : allStreaks) {
            User user = us.getUser();
            if (roleFilter != null && user.getRole() != roleFilter) continue;

            rank++;
            if (rank > limit) break;

            UserPoints points = pointsMap.get(user.getId());
            UserGameProfile profile = profileMap.get(user.getId());

            result.add(RankingEntryResponse.of(
                    rank, user.getId(), user.getName(),
                    user.getRole().getSimpleName(),
                    points != null ? points.getBalance() : 0,
                    profile != null ? profile.getLevel() : 1,
                    us.getCurrentStreak()
            ));
        }

        return result;
    }

    // ─── Helper ───

    private Map<Long, UserGameProfile> buildProfileMap() {
        return gameProfileRepository.findAll().stream()
                .collect(Collectors.toMap(gp -> gp.getUser().getId(), gp -> gp, (a, b) -> a));
    }

    private Map<Long, UserPoints> buildPointsMap() {
        return userPointsRepository.findAll().stream()
                .collect(Collectors.toMap(up -> up.getUser().getId(), up -> up, (a, b) -> a));
    }

    private Map<Long, UserStreak> buildStreakMap() {
        return userStreakRepository.findAll().stream()
                .collect(Collectors.toMap(us -> us.getUser().getId(), us -> us, (a, b) -> a));
    }

    private Role parseRole(String role) {
        if (role == null || role.isBlank()) return null;
        try {
            String normalized = role.toUpperCase();
            if (!normalized.startsWith("ROLE_")) {
                normalized = "ROLE_" + normalized;
            }
            return Role.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid role filter: {}", role);
            return null;
        }
    }
}

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
import com.jinsung.safety_road_inclass.domain.team.entity.MembershipStatus;
import com.jinsung.safety_road_inclass.domain.team.entity.Team;
import com.jinsung.safety_road_inclass.domain.team.entity.TeamMember;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamMemberRepository;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class RankingService {

    private final UserPointsRepository userPointsRepository;
    private final UserStreakRepository userStreakRepository;
    private final GameProfileRepository gameProfileRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    public List<RankingEntryResponse> getRankings(String type, int limit, String role) {
        Role roleFilter = parseRole(role);

        return switch (type) {
            case "level" -> getLevelRankings(limit, roleFilter);
            case "streak" -> getStreakRankings(limit, roleFilter);
            default -> getPointsRankings(limit, roleFilter);
        };
    }

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

    public List<TeamRankingResponse> getTeamRankings(String type, int limit) {
        Map<Long, UserPoints> pointsMap = buildPointsMap();
        List<TeamRankingResponse> teams = new ArrayList<>();

        for (Team team : teamRepository.findAll()) {
            List<TeamMember> activeMembers = teamMemberRepository.findAllByTeamAndStatus(team, MembershipStatus.ACTIVE);
            if (activeMembers.isEmpty()) {
                continue;
            }

            long totalPoints = activeMembers.stream()
                    .map(TeamMember::getUser)
                    .map(User::getId)
                    .map(pointsMap::get)
                    .filter(Objects::nonNull)
                    .mapToLong(UserPoints::getBalance)
                    .sum();
            int avgPoints = (int) (totalPoints / activeMembers.size());

            String topMember = activeMembers.stream()
                    .map(TeamMember::getUser)
                    .max(Comparator.comparingInt(user -> {
                        UserPoints userPoints = pointsMap.get(user.getId());
                        return userPoints != null ? userPoints.getBalance() : 0;
                    }))
                    .map(User::getName)
                    .orElse("");

            teams.add(TeamRankingResponse.of(
                    0,
                    team.getId(),
                    team.getName(),
                    activeMembers.size(),
                    totalPoints,
                    avgPoints,
                    topMember
            ));
        }

        if ("level".equals(type) || "streak".equals(type)) {
            teams.sort(Comparator.comparingInt(TeamRankingResponse::getAvgPoints).reversed());
        } else {
            teams.sort(Comparator.comparingLong(TeamRankingResponse::getTotalPoints).reversed());
        }

        List<TeamRankingResponse> result = new ArrayList<>();
        for (int i = 0; i < Math.min(teams.size(), limit); i++) {
            TeamRankingResponse team = teams.get(i);
            result.add(TeamRankingResponse.of(
                    i + 1,
                    team.getTeamId(),
                    team.getTeamName(),
                    team.getMemberCount(),
                    team.getTotalPoints(),
                    team.getAvgPoints(),
                    team.getTopMember()
            ));
        }

        return result;
    }

    private List<RankingEntryResponse> getPointsRankings(int limit, Role roleFilter) {
        List<UserPoints> allPoints = userPointsRepository.findAllOrderByBalanceDesc();
        Map<Long, UserGameProfile> profileMap = buildProfileMap();
        Map<Long, UserStreak> streakMap = buildStreakMap();

        List<RankingEntryResponse> result = new ArrayList<>();
        int rank = 0;

        for (UserPoints userPoints : allPoints) {
            User user = userPoints.getUser();
            if (roleFilter != null && user.getRole() != roleFilter) continue;

            rank++;
            if (rank > limit) break;

            UserGameProfile profile = profileMap.get(user.getId());
            UserStreak streak = streakMap.get(user.getId());

            result.add(RankingEntryResponse.of(
                    rank, user.getId(), user.getName(),
                    user.getRole().getSimpleName(),
                    userPoints.getBalance(),
                    profile != null ? profile.getLevel() : 1,
                    streak != null ? streak.getCurrentStreak() : 0
            ));
        }

        return result;
    }

    private List<RankingEntryResponse> getLevelRankings(int limit, Role roleFilter) {
        List<UserGameProfile> allProfiles = gameProfileRepository.findAllOrderByLevelDesc();
        Map<Long, UserPoints> pointsMap = buildPointsMap();
        Map<Long, UserStreak> streakMap = buildStreakMap();

        List<RankingEntryResponse> result = new ArrayList<>();
        int rank = 0;

        for (UserGameProfile profile : allProfiles) {
            User user = profile.getUser();
            if (roleFilter != null && user.getRole() != roleFilter) continue;

            rank++;
            if (rank > limit) break;

            UserPoints points = pointsMap.get(user.getId());
            UserStreak streak = streakMap.get(user.getId());

            result.add(RankingEntryResponse.of(
                    rank, user.getId(), user.getName(),
                    user.getRole().getSimpleName(),
                    points != null ? points.getBalance() : 0,
                    profile.getLevel(),
                    streak != null ? streak.getCurrentStreak() : 0
            ));
        }

        return result;
    }

    private List<RankingEntryResponse> getStreakRankings(int limit, Role roleFilter) {
        List<UserStreak> allStreaks = userStreakRepository.findAllOrderByCurrentStreakDesc();
        Map<Long, UserPoints> pointsMap = buildPointsMap();
        Map<Long, UserGameProfile> profileMap = buildProfileMap();

        List<RankingEntryResponse> result = new ArrayList<>();
        int rank = 0;

        for (UserStreak streak : allStreaks) {
            User user = streak.getUser();
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
                    streak.getCurrentStreak()
            ));
        }

        return result;
    }

    private Map<Long, UserGameProfile> buildProfileMap() {
        return gameProfileRepository.findAll().stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), profile -> profile, (a, b) -> a));
    }

    private Map<Long, UserPoints> buildPointsMap() {
        return userPointsRepository.findAll().stream()
                .collect(Collectors.toMap(points -> points.getUser().getId(), points -> points, (a, b) -> a));
    }

    private Map<Long, UserStreak> buildStreakMap() {
        return userStreakRepository.findAll().stream()
                .collect(Collectors.toMap(streak -> streak.getUser().getId(), streak -> streak, (a, b) -> a));
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

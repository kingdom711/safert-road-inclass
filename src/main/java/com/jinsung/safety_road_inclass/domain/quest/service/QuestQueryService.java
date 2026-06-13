package com.jinsung.safety_road_inclass.domain.quest.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.gameprofile.service.GameProfileService;
import com.jinsung.safety_road_inclass.domain.point.service.PointService;
import com.jinsung.safety_road_inclass.domain.quest.dto.ClaimTeamQuestRewardResponse;
import com.jinsung.safety_road_inclass.domain.quest.dto.TeamQuestListResponse;
import com.jinsung.safety_road_inclass.domain.quest.dto.TeamQuestMemberProgressResponse;
import com.jinsung.safety_road_inclass.domain.quest.dto.TeamQuestResponse;
import com.jinsung.safety_road_inclass.domain.quest.dto.TeamQuestSummaryResponse;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestConditionType;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestDefinition;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestProgress;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestScope;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestType;
import com.jinsung.safety_road_inclass.domain.quest.entity.TeamQuestProgress;
import com.jinsung.safety_road_inclass.domain.quest.repository.QuestDefinitionRepository;
import com.jinsung.safety_road_inclass.domain.quest.repository.QuestProgressRepository;
import com.jinsung.safety_road_inclass.domain.quest.repository.TeamQuestProgressRepository;
import com.jinsung.safety_road_inclass.domain.quest.util.PeriodKeyResolver;
import com.jinsung.safety_road_inclass.domain.team.entity.MembershipStatus;
import com.jinsung.safety_road_inclass.domain.team.entity.Team;
import com.jinsung.safety_road_inclass.domain.team.entity.TeamMember;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamMemberRepository;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamRepository;
import com.jinsung.safety_road_inclass.domain.workstop.repository.WorkStopReportRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestQueryService {

    // 골드 보상은 1:1 포인트로 적립되도록 통일됨
    private static final int POINTS_PER_GOLD = 1;

    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final QuestDefinitionRepository questDefinitionRepository;
    private final QuestProgressRepository questProgressRepository;
    private final TeamQuestProgressRepository teamQuestProgressRepository;
    private final PointService pointService;
    private final WorkStopReportRepository workStopReportRepository;
    private final GameProfileService gameProfileService;

    @Transactional
    public List<TeamQuestResponse> getCurrentTeamQuests(Long userId, String period) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Team team = user.getTeam();
        if (team == null || !teamMemberRepository.existsByUserAndStatus(user, MembershipStatus.ACTIVE)) {
            throw new CustomException(ErrorCode.TEAM_MEMBERSHIP_NOT_FOUND);
        }

        List<TeamMember> activeMembers = rewardableMembers(
                teamMemberRepository.findAllByTeamAndStatus(team, MembershipStatus.ACTIVE));
        QuestType typeFilter = parsePeriod(period);
        LocalDateTime now = LocalDateTime.now();

        return questDefinitionRepository.findAllByScopeAndActiveTrueOrderByIdAsc(QuestScope.TEAM).stream()
                .filter(quest -> typeFilter == null || quest.getType() == typeFilter)
                .map(quest -> toResponse(user, team, activeMembers, quest, now, true))
                .toList();
    }

    @Transactional
    public TeamQuestListResponse getCurrentTeamQuestList(Long userId, String period) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Team team = user.getTeam();
        if (team == null || !teamMemberRepository.existsByUserAndStatus(user, MembershipStatus.ACTIVE)) {
            return TeamQuestListResponse.builder()
                    .code("TEAM_NOT_ASSIGNED")
                    .quests(List.of())
                    .build();
        }

        return TeamQuestListResponse.builder()
                .code("OK")
                .quests(getCurrentTeamQuests(userId, period))
                .build();
    }

    @Transactional
    public TeamQuestSummaryResponse getCurrentTeamQuestSummary(Long userId) {
        TeamQuestListResponse response = getCurrentTeamQuestList(userId, "weekly");
        List<TeamQuestResponse> weeklyQuests = response.getQuests();
        int total = weeklyQuests.size();
        int completed = (int) weeklyQuests.stream()
                .filter(TeamQuestResponse::isCompleted)
                .count();
        int weeklyProgress = total == 0 ? 0 : (int) Math.round((completed * 100.0) / total);

        return TeamQuestSummaryResponse.builder()
                .weeklyProgress(weeklyProgress)
                .completedQuests(completed)
                .totalQuests(total)
                .build();
    }

    @Transactional
    public ClaimTeamQuestRewardResponse claimTeamQuestReward(Long userId, Long questId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Team team = user.getTeam();
        if (team == null || !teamMemberRepository.existsByUserAndStatus(user, MembershipStatus.ACTIVE)) {
            throw new CustomException(ErrorCode.TEAM_MEMBERSHIP_NOT_FOUND);
        }

        QuestDefinition quest = questDefinitionRepository.findById(questId)
                .filter(candidate -> candidate.getScope() == QuestScope.TEAM && candidate.isActive())
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        List<TeamMember> activeMembers = rewardableMembers(
                teamMemberRepository.findAllByTeamAndStatus(team, MembershipStatus.ACTIVE));
        LocalDateTime now = LocalDateTime.now();
        String periodKey = quest.getType() == QuestType.WEEKLY
                ? PeriodKeyResolver.weeklyKey(now)
                : PeriodKeyResolver.dailyKey(now);

        int completedCount = (int) activeMembers.stream()
                .map(TeamMember::getUser)
                .filter(member -> isCompleted(team, member, quest, periodKey, now))
                .count();

        if (activeMembers.isEmpty() || completedCount < activeMembers.size()) {
            throw new CustomException(ErrorCode.REWARD_NOT_ACTIVE);
        }

        TeamQuestProgress teamProgress = teamQuestProgressRepository.findByTeamAndQuestAndPeriodKey(team, quest, periodKey)
                .orElseGet(() -> TeamQuestProgress.builder()
                        .team(team)
                        .quest(quest)
                        .periodKey(periodKey)
                        .build());

        // 멱등성 보장: 이미 보상이 지급된 경우에도 성공 응답 반환 (자동 지급 폴백)
        if (teamProgress.getRewardedAt() != null) {
            return ClaimTeamQuestRewardResponse.builder()
                    .questId(quest.getId())
                    .code(quest.getCode())
                    .periodKey(periodKey)
                    .rewardedMembers(activeMembers.size())
                    .reward(toReward(quest))
                    .build();
        }

        teamProgress.updateCounts(completedCount, activeMembers.size());
        teamProgress.markRewarded();
        teamQuestProgressRepository.save(teamProgress);

        activeMembers.forEach(member -> grantReward(member.getUser(), quest));

        return ClaimTeamQuestRewardResponse.builder()
                .questId(quest.getId())
                .code(quest.getCode())
                .periodKey(periodKey)
                .rewardedMembers(activeMembers.size())
                .reward(toReward(quest))
                .build();
    }

    private TeamQuestResponse toResponse(User me, Team team, List<TeamMember> activeMembers,
                                         QuestDefinition quest, LocalDateTime now, boolean syncCompletion) {
        String periodKey = quest.getType() == QuestType.WEEKLY
                ? PeriodKeyResolver.weeklyKey(now)
                : PeriodKeyResolver.dailyKey(now);

        List<TeamQuestMemberProgressResponse> members = activeMembers.stream()
                .map(member -> toMemberResponse(me, member, quest, periodKey, now))
                .toList();

        int completedCount = (int) members.stream()
                .filter(TeamQuestMemberProgressResponse::isCompleted)
                .count();

        boolean completed = activeMembers.size() > 0 && completedCount >= activeMembers.size();
        TeamQuestProgress storedProgress = syncCompletion
                ? syncTeamProgress(team, activeMembers, quest, periodKey, completedCount, completed)
                : teamQuestProgressRepository.findByTeamAndQuestAndPeriodKey(team, quest, periodKey).orElse(null);
        boolean rewardClaimed = storedProgress != null && storedProgress.getRewardedAt() != null;

        return TeamQuestResponse.builder()
                .id(quest.getId())
                .code(quest.getCode())
                .title(quest.getTitle())
                .description(quest.getDescription())
                .type(quest.getType())
                .conditionType(quest.getConditionType())
                .periodKey(periodKey)
                .completedMemberCount(completedCount)
                .totalMemberCount(activeMembers.size())
                .completed(completed)
                .rewardClaimed(rewardClaimed)
                .reward(TeamQuestResponse.Reward.builder()
                        .points(getExchangeBackedRewardPoints(quest))
                        .exp(quest.getRewardExp())
                        .gold(0)
                        .build())
                .members(members)
                .build();
    }

    @Transactional
    public void closeWeeklyZeroWorkStopQuests(LocalDateTime now) {
        questDefinitionRepository.findAllByScopeAndActiveTrueOrderByIdAsc(QuestScope.TEAM).stream()
                .filter(quest -> quest.getType() == QuestType.WEEKLY)
                .filter(quest -> quest.getConditionType() == QuestConditionType.ZERO_WORK_STOP)
                .forEach(quest -> closeWeeklyZeroWorkStopQuest(now, quest));
    }

    private void closeWeeklyZeroWorkStopQuest(LocalDateTime now, QuestDefinition quest) {
        String periodKey = PeriodKeyResolver.weeklyKey(now);
        for (Team team : teamRepository.findAll()) {
            List<TeamMember> activeMembers = rewardableMembers(
                    teamMemberRepository.findAllByTeamAndStatus(team, MembershipStatus.ACTIVE));
            if (activeMembers.isEmpty()) {
                continue;
            }
            boolean completed = isZeroWorkStopCompleted(team, now);
            int completedCount = completed ? activeMembers.size() : 0;
            TeamQuestProgress teamProgress = syncTeamProgress(team, activeMembers, quest, periodKey, completedCount, completed);
            // 스케줄러 전용 보상 경로: zero_work_stop은 이벤트가 아닌 주간 마감 시 평가
            if (completed && teamProgress.getRewardedAt() == null) {
                activeMembers.forEach(member -> grantReward(member.getUser(), quest));
                teamProgress.markRewarded();
                teamQuestProgressRepository.save(teamProgress);
            }
        }
    }

    /**
     * 팀 퀘스트 진행도 동기화 (카운트만 갱신, 보상 지급 없음).
     * 보상은 QuestProgressListener(이벤트) 또는 closeWeeklyZeroWorkStopQuest(스케줄러)에서 처리.
     */
    private TeamQuestProgress syncTeamProgress(Team team, List<TeamMember> activeMembers, QuestDefinition quest,
                                               String periodKey, int completedCount, boolean completed) {
        TeamQuestProgress teamProgress = teamQuestProgressRepository.findByTeamAndQuestAndPeriodKey(team, quest, periodKey)
                .orElseGet(() -> TeamQuestProgress.builder()
                        .team(team)
                        .quest(quest)
                        .periodKey(periodKey)
                        .build());

        teamProgress.updateCounts(completedCount, activeMembers.size());
        return teamQuestProgressRepository.save(teamProgress);
    }

    private void grantReward(User user, QuestDefinition quest) {
        int rewardPoints = getExchangeBackedRewardPoints(quest);
        if (rewardPoints > 0) {
            pointService.addPoints(
                    user.getId(),
                    rewardPoints,
                    "팀 퀘스트 보상",
                    quest.getTitle() + " 완료 보상"
            );
        }
        if (quest.getRewardExp() > 0) {
            gameProfileService.addExp(user.getId(), quest.getRewardExp(), "팀 퀘스트 보상: " + quest.getTitle());
        }
    }

    private TeamQuestResponse.Reward toReward(QuestDefinition quest) {
        return TeamQuestResponse.Reward.builder()
                .points(getExchangeBackedRewardPoints(quest))
                .exp(quest.getRewardExp())
                .gold(0)
                .build();
    }

    private int getExchangeBackedRewardPoints(QuestDefinition quest) {
        return quest.getRewardPoints() + (quest.getRewardGold() * POINTS_PER_GOLD);
    }

    private List<TeamMember> rewardableMembers(List<TeamMember> members) {
        return members.stream()
                .filter(member -> {
                    User user = member.getUser();
                    return user != null
                            && user.getRole() != Role.ROLE_ADMIN
                            && user.getRole() != Role.ROLE_PROJECT_ADMIN;
                })
                .toList();
    }

    private TeamQuestMemberProgressResponse toMemberResponse(User me, TeamMember member,
                                                             QuestDefinition quest, String periodKey,
                                                             LocalDateTime now) {
        User memberUser = member.getUser();
        boolean completed = isCompleted(member.getTeam(), memberUser, quest, periodKey, now);

        return TeamQuestMemberProgressResponse.builder()
                .userId(memberUser.getId())
                .name(memberUser.getName())
                .me(memberUser.getId().equals(me.getId()))
                .completed(completed)
                .build();
    }

    private QuestType parsePeriod(String period) {
        if (period == null || period.isBlank() || "all".equalsIgnoreCase(period)) {
            return null;
        }
        return QuestType.valueOf(period.trim().toUpperCase(Locale.ROOT));
    }

    private boolean isCompleted(Team team, User user, QuestDefinition quest, String periodKey, LocalDateTime now) {
        if (quest.getConditionType() == QuestConditionType.ZERO_WORK_STOP) {
            return isZeroWorkStopCompleted(team, now);
        }
        if (quest.getConditionType() == QuestConditionType.DAILY_ALL_COMPLETE) {
            return isDailyAllComplete(team, user, periodKey, now);
        }
        return questProgressRepository.findByUserAndQuestAndPeriodKey(user, quest, periodKey)
                .map(QuestProgress::isCompleted)
                .orElse(false);
    }

    private boolean isDailyAllComplete(Team team, User user, String periodKey, LocalDateTime now) {
        List<QuestDefinition> dailyQuests = questDefinitionRepository.findAllByScopeAndActiveTrueOrderByIdAsc(QuestScope.TEAM).stream()
                .filter(quest -> quest.getType() == QuestType.DAILY)
                .filter(quest -> quest.getConditionType() != QuestConditionType.DAILY_ALL_COMPLETE)
                .toList();

        return !dailyQuests.isEmpty() && dailyQuests.stream()
                .allMatch(quest -> isCompleted(team, user, quest, periodKey, now));
    }

    private boolean isZeroWorkStopCompleted(Team team, LocalDateTime now) {
        LocalDateTime start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate()
                .atStartOfDay();
        LocalDateTime end = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).toLocalDate()
                .atTime(LocalTime.MAX);
        return workStopReportRepository.countByReporterTeamAndCreatedAtBetween(team, start, end) == 0;
    }
}

package com.jinsung.safety_road_inclass.domain.quest.listener;

import com.jinsung.safety_road_inclass.domain.alert.entity.AlertType;
import com.jinsung.safety_road_inclass.domain.alert.service.AlertService;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.gameprofile.service.GameProfileService;
import com.jinsung.safety_road_inclass.domain.point.service.PointService;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestConditionType;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestDefinition;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestProgress;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestScope;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestType;
import com.jinsung.safety_road_inclass.domain.quest.entity.TeamQuestProgress;
import com.jinsung.safety_road_inclass.domain.quest.event.EducationCompletedEvent;
import com.jinsung.safety_road_inclass.domain.quest.event.HazardReportedEvent;
import com.jinsung.safety_road_inclass.domain.quest.event.WorkStopReportedEvent;
import com.jinsung.safety_road_inclass.domain.quest.repository.QuestDefinitionRepository;
import com.jinsung.safety_road_inclass.domain.quest.repository.QuestProgressRepository;
import com.jinsung.safety_road_inclass.domain.quest.repository.TeamQuestProgressRepository;
import com.jinsung.safety_road_inclass.domain.quest.util.PeriodKeyResolver;
import com.jinsung.safety_road_inclass.domain.team.entity.MembershipStatus;
import com.jinsung.safety_road_inclass.domain.team.entity.Team;
import com.jinsung.safety_road_inclass.domain.team.entity.TeamMember;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuestProgressListener {

    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final QuestDefinitionRepository questDefinitionRepository;
    private final QuestProgressRepository questProgressRepository;
    private final TeamQuestProgressRepository teamQuestProgressRepository;
    private final PointService pointService;
    private final GameProfileService gameProfileService;
    private final AlertService alertService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onHazardReported(HazardReportedEvent event) {
        applyTeamQuestProgress(event.userId(), QuestConditionType.HAZARD_REPORTED, event.occurredAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEducationCompleted(EducationCompletedEvent event) {
        applyTeamQuestProgress(event.userId(), QuestConditionType.EDUCATION_COMPLETED, event.occurredAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onWorkStopReported(WorkStopReportedEvent event) {
        log.debug("Work-stop report observed for quest period evaluation: userId={}, reportId={}",
                event.userId(), event.reportId());
    }

    private void applyTeamQuestProgress(Long userId, QuestConditionType conditionType, LocalDateTime occurredAt) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getTeam() == null) {
            return;
        }

        Team team = user.getTeam();
        if (!teamMemberRepository.existsByUserAndStatus(user, MembershipStatus.ACTIVE)) {
            return;
        }

        List<TeamMember> activeMembers = teamMemberRepository.findAllByTeamAndStatus(team, MembershipStatus.ACTIVE);
        if (activeMembers.isEmpty()) {
            return;
        }

        questDefinitionRepository.findAllByScopeAndActiveTrueOrderByIdAsc(QuestScope.TEAM).stream()
                .filter(quest -> quest.getConditionType() == conditionType)
                .forEach(quest -> applyQuest(user, team, activeMembers, quest, occurredAt));
    }

    private void applyQuest(User user, Team team, List<TeamMember> activeMembers,
                            QuestDefinition quest, LocalDateTime occurredAt) {
        String periodKey = periodKey(quest, occurredAt);

        QuestProgress progress = questProgressRepository.findByUserAndQuestAndPeriodKey(user, quest, periodKey)
                .orElseGet(() -> QuestProgress.builder()
                        .user(user)
                        .quest(quest)
                        .periodKey(periodKey)
                        .build());
        progress.addProgress(1);
        questProgressRepository.save(progress);

        int completedCount = (int) activeMembers.stream()
                .map(TeamMember::getUser)
                .filter(member -> questProgressRepository.findByUserAndQuestAndPeriodKey(member, quest, periodKey)
                        .map(QuestProgress::isCompleted)
                        .orElse(false))
                .count();

        TeamQuestProgress teamProgress = teamQuestProgressRepository.findByTeamAndQuestAndPeriodKey(team, quest, periodKey)
                .orElseGet(() -> TeamQuestProgress.builder()
                        .team(team)
                        .quest(quest)
                        .periodKey(periodKey)
                        .build());
        teamProgress.updateCounts(completedCount, activeMembers.size());
        if (teamProgress.isCompleted() && teamProgress.getRewardedAt() == null) {
            rewardTeamMembers(activeMembers, quest);
            teamProgress.markRewarded();
        }
        teamQuestProgressRepository.save(teamProgress);
    }

    private void rewardTeamMembers(List<TeamMember> activeMembers, QuestDefinition quest) {
        for (TeamMember member : activeMembers) {
            User user = member.getUser();
            if (quest.getRewardPoints() > 0) {
                pointService.addPoints(user.getId(), quest.getRewardPoints(), "팀 퀘스트 보상",
                        quest.getTitle() + " 완료 보상");
            }
            if (quest.getRewardExp() > 0) {
                gameProfileService.addExp(user.getId(), quest.getRewardExp(), "팀 퀘스트 보상: " + quest.getTitle());
            }
            if (quest.getRewardGold() > 0) {
                // 골드 보상은 포인트 적립으로 통일 (보상센터 흐름은 별도 유지)
                pointService.addPoints(user.getId(), quest.getRewardGold(), "팀 퀘스트 보상(골드→포인트)",
                        quest.getTitle() + " 완료 보상");
            }
            alertService.createTeamNotification(
                    user,
                    member.getTeam().getLeader(),
                    AlertType.TEAM_QUEST_REWARDED,
                    "팀 퀘스트 보상 지급",
                    quest.getTitle() + " 완료 보상이 지급되었습니다."
            );
        }
    }

    private String periodKey(QuestDefinition quest, LocalDateTime occurredAt) {
        if (quest.getType() == QuestType.WEEKLY) {
            return PeriodKeyResolver.weeklyKey(occurredAt);
        }
        return PeriodKeyResolver.dailyKey(occurredAt);
    }
}

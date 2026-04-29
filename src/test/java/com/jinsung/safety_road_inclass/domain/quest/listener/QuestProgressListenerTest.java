package com.jinsung.safety_road_inclass.domain.quest.listener;

import com.jinsung.safety_road_inclass.domain.alert.service.AlertService;
import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.gameprofile.service.GameProfileService;
import com.jinsung.safety_road_inclass.domain.gold.service.GoldService;
import com.jinsung.safety_road_inclass.domain.point.service.PointService;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestConditionType;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestDefinition;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestProgress;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestScope;
import com.jinsung.safety_road_inclass.domain.quest.entity.QuestType;
import com.jinsung.safety_road_inclass.domain.quest.entity.TeamQuestProgress;
import com.jinsung.safety_road_inclass.domain.quest.event.HazardReportedEvent;
import com.jinsung.safety_road_inclass.domain.quest.repository.QuestDefinitionRepository;
import com.jinsung.safety_road_inclass.domain.quest.repository.QuestProgressRepository;
import com.jinsung.safety_road_inclass.domain.quest.repository.TeamQuestProgressRepository;
import com.jinsung.safety_road_inclass.domain.team.entity.MembershipStatus;
import com.jinsung.safety_road_inclass.domain.team.entity.Team;
import com.jinsung.safety_road_inclass.domain.team.entity.TeamMember;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestProgressListenerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private QuestDefinitionRepository questDefinitionRepository;
    @Mock
    private QuestProgressRepository questProgressRepository;
    @Mock
    private TeamQuestProgressRepository teamQuestProgressRepository;
    @Mock
    private PointService pointService;
    @Mock
    private GoldService goldService;
    @Mock
    private GameProfileService gameProfileService;
    @Mock
    private AlertService alertService;

    private QuestProgressListener listener;

    @BeforeEach
    void setUp() {
        listener = new QuestProgressListener(
                userRepository,
                teamMemberRepository,
                questDefinitionRepository,
                questProgressRepository,
                teamQuestProgressRepository,
                pointService,
                goldService,
                gameProfileService,
                alertService
        );
    }

    @Test
    void rewards_all_members_once_when_team_quest_completes() {
        User leader = user(1L, "leader");
        User first = user(2L, "first");
        User second = user(3L, "second");
        Team team = team(10L, leader);
        first.assignTeam(team);
        second.assignTeam(team);

        TeamMember firstMember = member(100L, team, first);
        TeamMember secondMember = member(101L, team, second);
        QuestDefinition quest = quest();
        String periodKey = "20260429";

        Map<Long, QuestProgress> progressStore = new HashMap<>();
        QuestProgress firstProgress = QuestProgress.builder()
                .user(first)
                .quest(quest)
                .periodKey(periodKey)
                .completed(true)
                .progress(1)
                .completedAt(LocalDateTime.now())
                .build();
        progressStore.put(first.getId(), firstProgress);

        when(userRepository.findById(second.getId())).thenReturn(Optional.of(second));
        when(teamMemberRepository.existsByUserAndStatus(second, MembershipStatus.ACTIVE)).thenReturn(true);
        when(teamMemberRepository.findAllByTeamAndStatus(team, MembershipStatus.ACTIVE))
                .thenReturn(List.of(firstMember, secondMember));
        when(questDefinitionRepository.findAllByScopeAndActiveTrueOrderByIdAsc(QuestScope.TEAM))
                .thenReturn(List.of(quest));
        when(questProgressRepository.findByUserAndQuestAndPeriodKey(any(User.class), eq(quest), eq(periodKey)))
                .thenAnswer(invocation -> Optional.ofNullable(progressStore.get(invocation.<User>getArgument(0).getId())));
        when(questProgressRepository.save(any(QuestProgress.class))).thenAnswer(invocation -> {
            QuestProgress progress = invocation.getArgument(0);
            progressStore.put(progress.getUser().getId(), progress);
            return progress;
        });
        when(teamQuestProgressRepository.findByTeamAndQuestAndPeriodKey(team, quest, periodKey))
                .thenReturn(Optional.empty());
        when(teamQuestProgressRepository.save(any(TeamQuestProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        listener.onHazardReported(new HazardReportedEvent(second.getId(), 999L, LocalDateTime.of(2026, 4, 29, 10, 0)));

        verify(pointService, times(2)).addPoints(anyLong(), eq(200), eq("팀 퀘스트 보상"), anyString());
        verify(gameProfileService, times(2)).addExp(anyLong(), eq(50), anyString());
        verify(goldService, never()).addGold(anyLong(), anyInt(), anyString(), anyString());
        verify(alertService, times(2)).createTeamNotification(any(User.class), eq(leader), any(), anyString(), anyString());
        verify(teamQuestProgressRepository).save(argThat(saved ->
                saved.isCompleted() && saved.getRewardedAt() != null
                        && saved.getCompletedMemberCount() == 2
                        && saved.getTotalMemberCount() == 2));
        assertThat(progressStore.get(second.getId()).isCompleted()).isTrue();
    }

    private User user(Long id, String name) {
        User user = User.builder().username(name).password("p").role(Role.ROLE_WORKER).name(name).build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Team team(Long id, User leader) {
        Team team = Team.builder().name("A팀").normalizedName("a팀").siteName("현장").leader(leader).build();
        ReflectionTestUtils.setField(team, "id", id);
        return team;
    }

    private TeamMember member(Long id, Team team, User user) {
        TeamMember member = TeamMember.builder()
                .team(team)
                .user(user)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private QuestDefinition quest() {
        QuestDefinition quest = QuestDefinition.builder()
                .code("team_hazard_relay")
                .title("위험 릴레이")
                .type(QuestType.DAILY)
                .scope(QuestScope.TEAM)
                .conditionType(QuestConditionType.HAZARD_REPORTED)
                .threshold(1)
                .rewardPoints(200)
                .rewardExp(50)
                .rewardGold(0)
                .active(true)
                .build();
        ReflectionTestUtils.setField(quest, "id", 20L);
        return quest;
    }
}

package com.jinsung.safety_road_inclass.domain.team.service;

import com.jinsung.safety_road_inclass.domain.alert.service.AlertService;
import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.team.entity.MembershipStatus;
import com.jinsung.safety_road_inclass.domain.team.entity.Team;
import com.jinsung.safety_road_inclass.domain.team.entity.TeamMember;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamMemberRepository;
import com.jinsung.safety_road_inclass.domain.team.repository.TeamRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamMembershipServiceTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AlertService alertService;

    private TeamMembershipService service;

    @BeforeEach
    void setUp() {
        service = new TeamMembershipService(teamRepository, teamMemberRepository, userRepository, alertService);
    }

    private User user(Long id, String name) {
        User u = User.builder().username(name).password("p").role(Role.ROLE_WORKER).name(name).build();
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private Team team(Long id, User leader) {
        Team t = Team.builder()
                .name("철근팀").normalizedName("철근팀").siteName("판교현장").leader(leader).build();
        ReflectionTestUtils.setField(t, "id", id);
        return t;
    }

    private TeamMember member(Long id, Team team, User user, MembershipStatus status) {
        TeamMember m = TeamMember.builder()
                .team(team).user(user).status(status).joinedAt(LocalDateTime.now()).build();
        ReflectionTestUtils.setField(m, "id", id);
        return m;
    }

    @Test
    @DisplayName("requestJoin: 신규 사용자가 PENDING 멤버십 생성")
    void requestJoin_creates_pending() {
        User leader = user(1L, "leader");
        User alice = user(2L, "alice");
        Team t = team(10L, leader);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(t));
        when(userRepository.findById(2L)).thenReturn(Optional.of(alice));
        when(teamMemberRepository.existsByUserAndStatus(alice, MembershipStatus.ACTIVE)).thenReturn(false);
        when(teamMemberRepository.findByTeamAndUser(t, alice)).thenReturn(Optional.empty());
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(inv -> inv.getArgument(0));

        var res = service.requestJoin(10L, 2L);
        assertThat(res.getStatus()).isEqualTo(MembershipStatus.PENDING);
    }

    @Test
    @DisplayName("requestJoin: 알림 생성이 실패해도 가입 신청은 PENDING으로 완료")
    void requestJoin_continues_when_notification_fails() {
        User leader = user(1L, "leader");
        User alice = user(2L, "alice");
        Team t = team(10L, leader);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(t));
        when(userRepository.findById(2L)).thenReturn(Optional.of(alice));
        when(teamMemberRepository.existsByUserAndStatus(alice, MembershipStatus.ACTIVE)).thenReturn(false);
        when(teamMemberRepository.findByTeamAndUser(t, alice)).thenReturn(Optional.empty());
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("alert table is out of sync"))
                .when(alertService)
                .createTeamNotification(any(), any(), any(), any(), any());

        var res = service.requestJoin(10L, 2L);

        assertThat(res.getStatus()).isEqualTo(MembershipStatus.PENDING);
    }

    @Test
    @DisplayName("requestJoin: 이미 ACTIVE 멤버십 보유 시 TEAM_ALREADY_JOINED")
    void requestJoin_blocked_when_active_elsewhere() {
        User leader = user(1L, "leader");
        User alice = user(2L, "alice");
        Team t = team(10L, leader);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(t));
        when(userRepository.findById(2L)).thenReturn(Optional.of(alice));
        when(teamMemberRepository.existsByUserAndStatus(alice, MembershipStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> service.requestJoin(10L, 2L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TEAM_ALREADY_JOINED);
    }

    @Test
    @DisplayName("requestJoin: LEFT 레코드 존재 시 PENDING으로 재활성")
    void requestJoin_reactivates_left() {
        User leader = user(1L, "leader");
        User alice = user(2L, "alice");
        Team t = team(10L, leader);
        TeamMember left = member(99L, t, alice, MembershipStatus.LEFT);
        ReflectionTestUtils.setField(left, "leftAt", LocalDateTime.now());

        when(teamRepository.findById(10L)).thenReturn(Optional.of(t));
        when(userRepository.findById(2L)).thenReturn(Optional.of(alice));
        when(teamMemberRepository.existsByUserAndStatus(alice, MembershipStatus.ACTIVE)).thenReturn(false);
        when(teamMemberRepository.findByTeamAndUser(t, alice)).thenReturn(Optional.of(left));

        var res = service.requestJoin(10L, 2L);
        assertThat(res.getStatus()).isEqualTo(MembershipStatus.PENDING);
        assertThat(left.getStatus()).isEqualTo(MembershipStatus.PENDING);
        verify(teamMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("approve: 리더가 PENDING을 ACTIVE로 승격, User.team_id 설정")
    void approve_success() {
        User leader = user(1L, "leader");
        User alice = user(2L, "alice");
        Team t = team(10L, leader);
        TeamMember pending = member(99L, t, alice, MembershipStatus.PENDING);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(t));
        when(userRepository.findById(2L)).thenReturn(Optional.of(alice));
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
        when(teamMemberRepository.findByTeamAndUser(t, alice)).thenReturn(Optional.of(pending));
        when(teamMemberRepository.existsByUserAndStatus(alice, MembershipStatus.ACTIVE)).thenReturn(false);

        service.approve(10L, 2L, 1L);

        assertThat(pending.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(pending.getApprovedBy()).isEqualTo(leader);
        assertThat(alice.getTeam()).isEqualTo(t);
    }

    @Test
    @DisplayName("approve: 리더가 아니면 TEAM_LEADER_FORBIDDEN")
    void approve_non_leader() {
        User leader = user(1L, "leader");
        User other = user(99L, "other");
        Team t = team(10L, leader);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> service.approve(10L, 2L, 99L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TEAM_LEADER_FORBIDDEN);
    }

    @Test
    @DisplayName("reject: PENDING 멤버십 삭제")
    void reject_deletes_pending() {
        User leader = user(1L, "leader");
        User alice = user(2L, "alice");
        Team t = team(10L, leader);
        TeamMember pending = member(99L, t, alice, MembershipStatus.PENDING);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(t));
        when(userRepository.findById(2L)).thenReturn(Optional.of(alice));
        when(teamMemberRepository.findByTeamAndUser(t, alice)).thenReturn(Optional.of(pending));

        service.reject(10L, 2L, 1L);
        verify(teamMemberRepository).delete(pending);
    }

    @Test
    @DisplayName("leave: 일반 멤버 탈퇴 시 LEFT, User.team null")
    void leave_member() {
        User leader = user(1L, "leader");
        User alice = user(2L, "alice");
        Team t = team(10L, leader);
        TeamMember active = member(99L, t, alice, MembershipStatus.ACTIVE);
        alice.assignTeam(t);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(t));
        when(userRepository.findById(2L)).thenReturn(Optional.of(alice));
        when(teamMemberRepository.findByTeamAndUser(t, alice)).thenReturn(Optional.of(active));

        service.leave(10L, 2L);
        assertThat(active.getStatus()).isEqualTo(MembershipStatus.LEFT);
        assertThat(alice.getTeam()).isNull();
    }

    @Test
    @DisplayName("leave: 리더는 직접 탈퇴 불가 (TEAM_LEADER_FORBIDDEN)")
    void leave_leader_blocked() {
        User leader = user(1L, "leader");
        Team t = team(10L, leader);
        TeamMember active = member(99L, t, leader, MembershipStatus.ACTIVE);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(t));
        when(userRepository.findById(1L)).thenReturn(Optional.of(leader));
        when(teamMemberRepository.findByTeamAndUser(t, leader)).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> service.leave(10L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TEAM_LEADER_FORBIDDEN);
    }

    @Test
    @DisplayName("kick: 리더가 다른 ACTIVE 멤버 강퇴")
    void kick_success() {
        User leader = user(1L, "leader");
        User alice = user(2L, "alice");
        Team t = team(10L, leader);
        TeamMember active = member(99L, t, alice, MembershipStatus.ACTIVE);
        alice.assignTeam(t);

        when(teamRepository.findById(10L)).thenReturn(Optional.of(t));
        when(userRepository.findById(2L)).thenReturn(Optional.of(alice));
        when(teamMemberRepository.findByTeamAndUser(t, alice)).thenReturn(Optional.of(active));

        service.kick(10L, 2L, 1L);
        assertThat(active.getStatus()).isEqualTo(MembershipStatus.LEFT);
        assertThat(alice.getTeam()).isNull();
    }

    @Test
    @DisplayName("kick: 리더 자신을 강퇴할 수 없음")
    void kick_self_blocked() {
        User leader = user(1L, "leader");
        Team t = team(10L, leader);
        when(teamRepository.findById(10L)).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> service.kick(10L, 1L, 1L))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TEAM_LEADER_FORBIDDEN);
    }
}

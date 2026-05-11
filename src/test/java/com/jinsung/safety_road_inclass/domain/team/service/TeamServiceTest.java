package com.jinsung.safety_road_inclass.domain.team.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.domain.team.dto.TeamCreateRequest;
import com.jinsung.safety_road_inclass.domain.team.dto.TeamSummaryResponse;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private UserRepository userRepository;

    private TeamService teamService;

    @BeforeEach
    void setUp() {
        teamService = new TeamService(teamRepository, teamMemberRepository, userRepository);
    }

    private User newUser(Long id, String username) {
        User user = User.builder()
                .username(username)
                .password("password")
                .role(Role.ROLE_WORKER)
                .name(username)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    @DisplayName("create: 정상 생성 — 정규화 이름 저장, 생성자가 ACTIVE 리더로 등록")
    void create_success() {
        User creator = newUser(10L, "leader1");
        when(userRepository.findById(10L)).thenReturn(Optional.of(creator));
        when(teamRepository.existsBySiteNameAndNormalizedName("판교현장", "철근팀 a조")).thenReturn(false);
        when(teamMemberRepository.existsByUserAndStatus(creator, MembershipStatus.ACTIVE)).thenReturn(false);
        when(teamRepository.saveAndFlush(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));
        when(teamMemberRepository.saveAndFlush(any(TeamMember.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        TeamCreateRequest req = new TeamCreateRequest("  철근팀  A조  ", "판교현장");
        TeamSummaryResponse res = teamService.create(10L, req);

        ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).saveAndFlush(teamCaptor.capture());
        Team saved = teamCaptor.getValue();
        assertThat(saved.getName()).isEqualTo("철근팀  A조".trim());
        assertThat(saved.getNormalizedName()).isEqualTo("철근팀 a조");
        assertThat(saved.getSiteName()).isEqualTo("판교현장");
        assertThat(saved.getLeader()).isEqualTo(creator);

        ArgumentCaptor<TeamMember> memberCaptor = ArgumentCaptor.forClass(TeamMember.class);
        verify(teamMemberRepository).saveAndFlush(memberCaptor.capture());
        TeamMember leaderMembership = memberCaptor.getValue();
        assertThat(leaderMembership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(leaderMembership.getApprovedBy()).isEqualTo(creator);

        assertThat(creator.getTeam()).isEqualTo(saved);
        assertThat(res.getActiveMemberCount()).isEqualTo(1L);
        verify(userRepository).saveAndFlush(creator);
    }

    @Test
    @DisplayName("create: 동일 현장 + 정규화 이름 중복 시 TEAM_NAME_DUPLICATED")
    void create_duplicateName() {
        when(teamRepository.existsBySiteNameAndNormalizedName("판교현장", "철근팀")).thenReturn(true);

        TeamCreateRequest req = new TeamCreateRequest("철근팀", "판교현장");
        assertThatThrownBy(() -> teamService.create(10L, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TEAM_NAME_DUPLICATED);

        verify(teamRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: 이미 ACTIVE 멤버십이 있으면 TEAM_ALREADY_JOINED")
    void create_alreadyJoined() {
        User creator = newUser(10L, "leader1");
        when(userRepository.findById(10L)).thenReturn(Optional.of(creator));
        when(teamRepository.existsBySiteNameAndNormalizedName("판교현장", "철근팀")).thenReturn(false);
        when(teamMemberRepository.existsByUserAndStatus(creator, MembershipStatus.ACTIVE)).thenReturn(true);

        TeamCreateRequest req = new TeamCreateRequest("철근팀", "판교현장");
        assertThatThrownBy(() -> teamService.create(10L, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TEAM_ALREADY_JOINED);
    }

    @Test
    @DisplayName("create: name 또는 siteName 공백이면 TEAM_INVALID_NAME")
    void create_blankName() {
        TeamCreateRequest req = new TeamCreateRequest("   ", "판교현장");
        assertThatThrownBy(() -> teamService.create(10L, req))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.TEAM_INVALID_NAME);
    }

    @Test
    @DisplayName("search: 쿼리 비어있으면 site 단독 조회, 있으면 정규화 후 LIKE 조회")
    void search_routesByQueryPresence() {
        User leader = newUser(1L, "L");
        Team t1 = Team.builder().name("A").normalizedName("a").siteName("판교현장").leader(leader).build();
        ReflectionTestUtils.setField(t1, "id", 100L);

        when(teamRepository.findBySiteNameOrderByNameAsc("판교현장")).thenReturn(List.of(t1));
        when(teamMemberRepository.countByTeamAndStatusAndUserRoleNotIn(
                eq(t1),
                eq(MembershipStatus.ACTIVE),
                eq(List.of(Role.ROLE_ADMIN, Role.ROLE_PROJECT_ADMIN))
        )).thenReturn(3L);

        List<TeamSummaryResponse> res = teamService.search("판교현장", null);
        assertThat(res).hasSize(1);
        assertThat(res.get(0).getActiveMemberCount()).isEqualTo(3L);

        // with query
        when(teamRepository.findBySiteNameAndNormalizedNameContainingOrderByNameAsc("판교현장", "철근"))
                .thenReturn(List.of(t1));
        teamService.search("판교현장", "  철근  ");
        verify(teamRepository).findBySiteNameAndNormalizedNameContainingOrderByNameAsc("판교현장", "철근");
    }

    @Test
    @DisplayName("search: siteName 누락 시 MISSING_PARAMETER")
    void search_missingSite() {
        assertThatThrownBy(() -> teamService.search("  ", "철근"))
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(ErrorCode.MISSING_PARAMETER);
    }
}

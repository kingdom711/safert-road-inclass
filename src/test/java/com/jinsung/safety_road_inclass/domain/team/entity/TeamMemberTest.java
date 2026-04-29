package com.jinsung.safety_road_inclass.domain.team.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamMemberTest {

    private User newUser(String username) {
        return User.builder()
                .username(username)
                .password("password")
                .role(Role.ROLE_WORKER)
                .name(username)
                .build();
    }

    private Team newTeam(User leader) {
        return Team.builder()
                .name("철근팀 A조")
                .normalizedName("철근팀 a조")
                .siteName("판교현장")
                .leader(leader)
                .build();
    }

    @Test
    @DisplayName("기본 생성 시 상태는 PENDING이며 joinedAt이 채워진다")
    void defaultStatusIsPending() {
        User leader = newUser("leader");
        User applicant = newUser("alice");
        Team team = newTeam(leader);

        TeamMember member = TeamMember.builder()
                .team(team)
                .user(applicant)
                .build();

        assertThat(member.getStatus()).isEqualTo(MembershipStatus.PENDING);
        assertThat(member.getJoinedAt()).isNotNull();
        assertThat(member.isPending()).isTrue();
        assertThat(member.isActive()).isFalse();
    }

    @Test
    @DisplayName("approve 호출 시 ACTIVE로 전이되고 승인 메타가 채워진다")
    void approveTransitionsToActive() {
        User leader = newUser("leader");
        User applicant = newUser("alice");
        Team team = newTeam(leader);
        TeamMember member = TeamMember.builder().team(team).user(applicant).build();

        member.approve(leader);

        assertThat(member.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
        assertThat(member.getApprovedAt()).isNotNull();
        assertThat(member.getApprovedBy()).isEqualTo(leader);
        assertThat(member.isActive()).isTrue();
    }

    @Test
    @DisplayName("PENDING이 아닌 상태에서 approve를 호출하면 예외가 발생한다")
    void approveFromNonPendingThrows() {
        User leader = newUser("leader");
        User applicant = newUser("alice");
        Team team = newTeam(leader);
        TeamMember member = TeamMember.builder()
                .team(team)
                .user(applicant)
                .status(MembershipStatus.ACTIVE)
                .build();

        assertThatThrownBy(() -> member.approve(leader))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("leave 호출 시 LEFT로 전이되고 leftAt이 채워진다")
    void leaveTransitionsToLeft() {
        User leader = newUser("leader");
        User applicant = newUser("alice");
        Team team = newTeam(leader);
        TeamMember member = TeamMember.builder()
                .team(team)
                .user(applicant)
                .status(MembershipStatus.ACTIVE)
                .build();

        member.leave();

        assertThat(member.getStatus()).isEqualTo(MembershipStatus.LEFT);
        assertThat(member.getLeftAt()).isNotNull();
    }
}

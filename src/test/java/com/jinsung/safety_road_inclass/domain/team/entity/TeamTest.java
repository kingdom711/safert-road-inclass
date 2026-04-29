package com.jinsung.safety_road_inclass.domain.team.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeamTest {

    private User newUser(String username) {
        return User.builder()
                .username(username)
                .password("password")
                .role(Role.ROLE_WORKER)
                .name(username)
                .build();
    }

    @Test
    @DisplayName("rename은 name과 normalizedName을 함께 갱신한다")
    void renameUpdatesBothNames() {
        Team team = Team.builder()
                .name("철근팀 A조")
                .normalizedName("철근팀 a조")
                .siteName("판교현장")
                .leader(newUser("leader"))
                .build();

        team.rename("철근팀 B조", "철근팀 b조");

        assertThat(team.getName()).isEqualTo("철근팀 B조");
        assertThat(team.getNormalizedName()).isEqualTo("철근팀 b조");
    }

    @Test
    @DisplayName("changeLeader는 리더 사용자를 교체한다")
    void changeLeaderReplacesLeader() {
        User original = newUser("leader1");
        User next = newUser("leader2");
        Team team = Team.builder()
                .name("철근팀")
                .normalizedName("철근팀")
                .siteName("판교현장")
                .leader(original)
                .build();

        team.changeLeader(next);

        assertThat(team.getLeader()).isEqualTo(next);
    }

    @Test
    @DisplayName("User.assignTeam 후 clearTeam을 호출하면 team 참조가 제거된다")
    void userAssignAndClearTeam() {
        User user = newUser("alice");
        Team team = Team.builder()
                .name("철근팀")
                .normalizedName("철근팀")
                .siteName("판교현장")
                .leader(newUser("leader"))
                .build();

        user.assignTeam(team);
        assertThat(user.getTeam()).isEqualTo(team);

        user.clearTeam();
        assertThat(user.getTeam()).isNull();
    }
}

package com.jinsung.safety_road_inclass.domain.auth.entity;

import com.jinsung.safety_road_inclass.domain.team.entity.Team;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * User - 사용자 Entity
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 100)
    private String email;

    @Column(nullable = false)
    private boolean isVerified = false;

    @Column(unique = true, length = 20)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Builder
    public User(String username, String password, Role role, String name, String email, boolean isVerified,
            String phoneNumber) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.email = email;
        this.isVerified = isVerified;
        this.phoneNumber = phoneNumber;
    }

    /**
     * 비밀번호 암호화
     */
    public void encodePassword(PasswordEncoder encoder) {
        this.password = encoder.encode(this.password);
    }

    /**
     * 비밀번호 검증
     */
    public boolean matchesPassword(PasswordEncoder encoder, String rawPassword) {
        return encoder.matches(rawPassword, this.password);
    }

    public void verify(String phoneNumber) {
        this.isVerified = true;
        this.phoneNumber = phoneNumber;
    }

    public void assignTeam(Team team) {
        this.team = team;
    }

    public void clearTeam() {
        this.team = null;
    }
}

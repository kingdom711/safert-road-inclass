package com.jinsung.safety_road_inclass.domain.auth.entity;

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

    @Builder
    public User(String username, String password, Role role, String name, String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.email = email;
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
}


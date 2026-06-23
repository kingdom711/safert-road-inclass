package com.jinsung.safety_road_inclass.domain.auth.entity;

import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_codes", indexes = {
        @Index(name = "idx_password_reset_codes_user", columnList = "user_id"),
        @Index(name = "idx_password_reset_codes_expires_at", columnList = "expires_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetCode extends BaseTimeEntity {

    private static final int MAX_ATTEMPTS = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "code_hash", nullable = false, length = 128)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;

    @Builder
    public PasswordResetCode(User user, String codeHash, LocalDateTime expiresAt) {
        this.user = user;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
    }

    public boolean isUsable(LocalDateTime now) {
        return usedAt == null && expiresAt.isAfter(now) && attemptCount < MAX_ATTEMPTS;
    }

    public boolean matches(String candidateHash) {
        return codeHash.equals(candidateHash);
    }

    public void recordFailedAttempt() {
        this.attemptCount += 1;
    }

    public void markUsed(LocalDateTime now) {
        this.usedAt = now;
    }

    public void expire(LocalDateTime now) {
        if (this.usedAt == null) {
            this.usedAt = now;
        }
    }
}

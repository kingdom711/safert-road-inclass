package com.jinsung.safety_road_inclass.domain.auth.entity;

import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_approval_requests", indexes = {
        @Index(name = "idx_password_reset_approval_user", columnList = "user_id"),
        @Index(name = "idx_password_reset_approval_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetApprovalRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "requested_email", nullable = false, length = 100)
    private String requestedEmail;

    @Column(name = "encoded_new_password", nullable = false)
    private String encodedNewPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PasswordResetApprovalStatus status = PasswordResetApprovalStatus.PENDING;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed_by")
    private Long processedBy;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @Builder
    public PasswordResetApprovalRequest(User user, String requestedEmail, String encodedNewPassword) {
        this.user = user;
        this.requestedEmail = requestedEmail;
        this.encodedNewPassword = encodedNewPassword;
    }

    public boolean isPending() {
        return status == PasswordResetApprovalStatus.PENDING;
    }

    public void approve(Long adminId, LocalDateTime now) {
        this.status = PasswordResetApprovalStatus.APPROVED;
        this.processedBy = adminId;
        this.processedAt = now;
    }

    public void reject(Long adminId, String reason, LocalDateTime now) {
        this.status = PasswordResetApprovalStatus.REJECTED;
        this.processedBy = adminId;
        this.rejectReason = reason;
        this.processedAt = now;
    }
}

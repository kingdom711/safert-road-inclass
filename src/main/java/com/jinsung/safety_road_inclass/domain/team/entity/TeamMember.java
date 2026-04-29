package com.jinsung.safety_road_inclass.domain.team.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "team_members",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_team_members_team_user",
                        columnNames = {"team_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "idx_team_members_user_status", columnList = "user_id,status"),
                @Index(name = "idx_team_members_team_status", columnList = "team_id,status")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MembershipStatus status;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Builder
    public TeamMember(Team team, User user, MembershipStatus status, LocalDateTime joinedAt) {
        this.team = team;
        this.user = user;
        this.status = status != null ? status : MembershipStatus.PENDING;
        this.joinedAt = joinedAt != null ? joinedAt : LocalDateTime.now();
    }

    public void approve(User approver) {
        if (this.status != MembershipStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태의 멤버십만 승인할 수 있습니다.");
        }
        this.status = MembershipStatus.ACTIVE;
        this.approvedAt = LocalDateTime.now();
        this.approvedBy = approver;
    }

    public void leave() {
        this.status = MembershipStatus.LEFT;
        this.leftAt = LocalDateTime.now();
    }

    /**
     * LEFT 상태에서 다시 신청할 때 PENDING으로 되돌린다.
     * 승인 메타는 새 승인 시점에 다시 채워진다.
     */
    public void requestRejoin() {
        if (this.status != MembershipStatus.LEFT) {
            throw new IllegalStateException("LEFT 상태에서만 재신청할 수 있습니다.");
        }
        this.status = MembershipStatus.PENDING;
        this.joinedAt = LocalDateTime.now();
        this.approvedAt = null;
        this.approvedBy = null;
        this.leftAt = null;
    }

    public boolean isActive() {
        return this.status == MembershipStatus.ACTIVE;
    }

    public boolean isPending() {
        return this.status == MembershipStatus.PENDING;
    }
}

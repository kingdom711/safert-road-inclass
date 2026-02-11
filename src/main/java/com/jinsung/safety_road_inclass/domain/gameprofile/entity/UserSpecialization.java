package com.jinsung.safety_road_inclass.domain.gameprofile.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * UserSpecialization - 유저별 해금된 전직(특수역할) Entity (1:N)
 */
@Entity
@Table(name = "user_specializations", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "spec_id" }))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSpecialization extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "spec_id", nullable = false, length = 50)
    private String specId;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    @Column(name = "education_progress", columnDefinition = "TEXT")
    private String educationProgress;

    @Builder
    public UserSpecialization(User user, String specId, LocalDateTime unlockedAt, String educationProgress) {
        this.user = user;
        this.specId = specId;
        this.unlockedAt = unlockedAt;
        this.educationProgress = educationProgress;
    }

    public void updateEducationProgress(String educationProgress) {
        this.educationProgress = educationProgress;
    }
}

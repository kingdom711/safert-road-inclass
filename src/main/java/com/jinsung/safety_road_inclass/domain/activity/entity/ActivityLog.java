package com.jinsung.safety_road_inclass.domain.activity.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ActivityLog - 통합 활동 기록 Entity
 */
@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_log_user_id", columnList = "user_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 50)
    private ActivityType activityType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Builder
    public ActivityLog(User user, ActivityType activityType, String title, String description, String metadata) {
        this.user = user;
        this.activityType = activityType;
        this.title = title;
        this.description = description;
        this.metadata = metadata;
    }
}

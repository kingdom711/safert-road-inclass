package com.jinsung.safety_road_inclass.domain.quest.entity;

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
        name = "quest_progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_quest_progress_user_quest_period",
                columnNames = {"user_id", "quest_id", "period_key"}
        ),
        indexes = {
                @Index(name = "idx_quest_progress_user_period", columnList = "user_id,period_key"),
                @Index(name = "idx_quest_progress_quest_period", columnList = "quest_id,period_key")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestProgress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quest_id", nullable = false)
    private QuestDefinition quest;

    @Column(name = "period_key", nullable = false, length = 20)
    private String periodKey;

    @Column(nullable = false)
    private boolean completed;

    @Column(nullable = false)
    private int progress;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    public QuestProgress(User user, QuestDefinition quest, String periodKey, Boolean completed, Integer progress,
                         LocalDateTime completedAt) {
        this.user = user;
        this.quest = quest;
        this.periodKey = periodKey;
        this.completed = completed != null && completed;
        this.progress = progress != null ? progress : 0;
        this.completedAt = completedAt;
    }

    public void addProgress(int amount) {
        if (completed) {
            return;
        }
        this.progress += amount;
        if (this.progress >= quest.getThreshold()) {
            this.completed = true;
            this.completedAt = LocalDateTime.now();
        }
    }
}

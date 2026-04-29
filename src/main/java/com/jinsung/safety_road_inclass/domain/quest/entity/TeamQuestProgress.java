package com.jinsung.safety_road_inclass.domain.quest.entity;

import com.jinsung.safety_road_inclass.domain.team.entity.Team;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "team_quest_progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_team_quest_progress_team_quest_period",
                columnNames = {"team_id", "quest_id", "period_key"}
        ),
        indexes = {
                @Index(name = "idx_team_quest_progress_team_period", columnList = "team_id,period_key"),
                @Index(name = "idx_team_quest_progress_quest_period", columnList = "quest_id,period_key")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamQuestProgress extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quest_id", nullable = false)
    private QuestDefinition quest;

    @Column(name = "period_key", nullable = false, length = 20)
    private String periodKey;

    @Column(nullable = false)
    private int completedMemberCount;

    @Column(nullable = false)
    private int totalMemberCount;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "rewarded_at")
    private LocalDateTime rewardedAt;

    @Builder
    public TeamQuestProgress(Team team, QuestDefinition quest, String periodKey, Integer completedMemberCount,
                             Integer totalMemberCount, Boolean completed, LocalDateTime rewardedAt) {
        this.team = team;
        this.quest = quest;
        this.periodKey = periodKey;
        this.completedMemberCount = completedMemberCount != null ? completedMemberCount : 0;
        this.totalMemberCount = totalMemberCount != null ? totalMemberCount : 0;
        this.completed = completed != null && completed;
        this.rewardedAt = rewardedAt;
    }

    public void updateCounts(int completedMemberCount, int totalMemberCount) {
        this.completedMemberCount = completedMemberCount;
        this.totalMemberCount = totalMemberCount;
        this.completed = totalMemberCount > 0 && completedMemberCount >= totalMemberCount;
    }

    public void markRewarded() {
        this.rewardedAt = LocalDateTime.now();
    }
}

package com.jinsung.safety_road_inclass.domain.quest.entity;

import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "quest_definitions",
        uniqueConstraints = @UniqueConstraint(name = "uk_quest_definitions_code", columnNames = "code"),
        indexes = {
                @Index(name = "idx_quest_definitions_scope_active", columnList = "scope,active"),
                @Index(name = "idx_quest_definitions_type_scope", columnList = "type,scope")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestDefinition extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestScope scope;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 60)
    private QuestConditionType conditionType;

    @Column(nullable = false)
    private int threshold;

    @Column(nullable = false)
    private int rewardPoints;

    @Column(nullable = false)
    private int rewardExp;

    @Column(nullable = false)
    private int rewardGold;

    @Column(nullable = false)
    private boolean active;

    @Builder
    public QuestDefinition(String code, String title, String description, QuestType type, QuestScope scope,
                           QuestConditionType conditionType, Integer threshold, Integer rewardPoints,
                           Integer rewardExp, Integer rewardGold, Boolean active) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.type = type;
        this.scope = scope;
        this.conditionType = conditionType;
        this.threshold = threshold != null ? threshold : 1;
        this.rewardPoints = rewardPoints != null ? rewardPoints : 0;
        this.rewardExp = rewardExp != null ? rewardExp : 0;
        this.rewardGold = rewardGold != null ? rewardGold : 0;
        this.active = active == null || active;
    }
}

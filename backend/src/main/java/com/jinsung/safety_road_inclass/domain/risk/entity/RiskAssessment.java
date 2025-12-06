package com.jinsung.safety_road_inclass.domain.risk.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.checklist.entity.ChecklistItem;
import com.jinsung.safety_road_inclass.domain.risk.service.RiskCalculator;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * RiskAssessment - 위험성 평가 Entity
 */
@Entity
@Table(name = "risk_assessments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RiskAssessment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "checklist_item_id", nullable = false, unique = true)
    private ChecklistItem checklistItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessed_by", nullable = false)
    private User assessedBy;

    @Column(nullable = false)
    private Integer frequency; // 1-5

    @Column(nullable = false)
    private Integer severity;  // 1-5

    @Column(nullable = false)
    private Integer riskScore; // frequency * severity

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime assessedAt;

    @OneToMany(mappedBy = "riskAssessment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Countermeasure> countermeasures = new ArrayList<>();

    @Builder
    public RiskAssessment(ChecklistItem checklistItem, User assessedBy,
                          Integer frequency, Integer severity, String description) {
        this.checklistItem = checklistItem;
        this.assessedBy = assessedBy;
        this.frequency = frequency;
        this.severity = severity;
        this.description = description;
        this.assessedAt = LocalDateTime.now();
        
        // 위험도 자동 계산
        this.riskScore = RiskCalculator.calculateScore(frequency, severity);
        this.riskLevel = RiskCalculator.determineLevel(this.riskScore);
    }

    /**
     * 대책 추가
     */
    public void addCountermeasure(Countermeasure countermeasure) {
        this.countermeasures.add(countermeasure);
        countermeasure.setRiskAssessment(this);
    }
}


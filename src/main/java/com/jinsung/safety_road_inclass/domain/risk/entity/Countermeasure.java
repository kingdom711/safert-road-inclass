package com.jinsung.safety_road_inclass.domain.risk.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Countermeasure - 개선 대책 Entity
 */
@Entity
@Table(name = "countermeasures")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Countermeasure extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "risk_assessment_id", nullable = false)
    private RiskAssessment riskAssessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CountermeasureStatus status = CountermeasureStatus.PLANNED;

    private LocalDate dueDate;

    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "countermeasure", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActionRecord> actionRecords = new ArrayList<>();

    @Builder
    public Countermeasure(User createdBy, String content, LocalDate dueDate) {
        this.createdBy = createdBy;
        this.content = content;
        this.dueDate = dueDate;
    }

    void setRiskAssessment(RiskAssessment riskAssessment) {
        this.riskAssessment = riskAssessment;
    }

    /**
     * 비즈니스 메서드: 진행 중으로 변경
     */
    public void start() {
        if (this.status != CountermeasureStatus.PLANNED) {
            throw new IllegalStateException("계획된 대책만 진행할 수 있습니다.");
        }
        this.status = CountermeasureStatus.IN_PROGRESS;
    }

    /**
     * 비즈니스 메서드: 완료 처리
     */
    public void complete() {
        if (this.status == CountermeasureStatus.COMPLETED) {
            throw new IllegalStateException("이미 완료된 대책입니다.");
        }
        this.status = CountermeasureStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * 조치 기록 추가
     */
    public void addActionRecord(ActionRecord actionRecord) {
        this.actionRecords.add(actionRecord);
        actionRecord.setCountermeasure(this);
    }
}


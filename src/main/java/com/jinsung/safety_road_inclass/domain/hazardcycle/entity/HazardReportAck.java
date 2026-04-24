package com.jinsung.safety_road_inclass.domain.hazardcycle.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * HazardReportAck - 동료 근로자 위험요인 확인 (산안법 제36조 2항 근로자 참여 증빙).
 *
 * 같은 사이클에 대해 동일 사용자가 중복 확인하지 못하도록 unique 제약.
 */
@Entity
@Table(name = "hazard_report_acks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_hazard_ack_report_user",
                columnNames = {"hazard_report_id", "acker_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HazardReportAck extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hazard_report_id", nullable = false)
    private HazardReport hazardReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acker_id", nullable = false)
    private User acker;

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "acked_at", nullable = false)
    private LocalDateTime ackedAt;

    @Builder
    public HazardReportAck(HazardReport hazardReport, User acker, String comment, LocalDateTime ackedAt) {
        this.hazardReport = hazardReport;
        this.acker = acker;
        this.comment = comment;
        this.ackedAt = ackedAt != null ? ackedAt : LocalDateTime.now();
    }
}

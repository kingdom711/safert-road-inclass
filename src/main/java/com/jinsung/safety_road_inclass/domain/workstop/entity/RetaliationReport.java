package com.jinsung.safety_road_inclass.domain.workstop.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * RetaliationReport - 보복 행위 신고 Entity
 *
 * 산업안전보건법 제52조 제3항 위반(불이익 처우) 증거 기록
 * - 작업중지권 행사 후 보호 기간 내 발생한 보복 행위 기록
 * - 중대재해 발생 시 사업주 의무 불이행 증거로 활용
 */
@Entity
@Table(name = "retaliation_reports", indexes = {
        @Index(name = "idx_rr_worker_id", columnList = "worker_id"),
        @Index(name = "idx_rr_protection_id", columnList = "protection_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RetaliationReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 보복 피해 근로자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    /** 관련 보호 기간 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protection_id", nullable = false)
    private WorkStopProtection protection;

    /** 보복 유형 (해고, 전보, 임금삭감, 기타) */
    @Column(nullable = false, length = 50)
    private String retaliationType;

    /** 보복 상세 설명 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /** 증거 자료 URL */
    @Column(length = 500)
    private String evidenceUrl;

    /** 처리 상태 */
    @Column(nullable = false, length = 20)
    private String status = "REPORTED";

    @Builder
    public RetaliationReport(User worker, WorkStopProtection protection,
                              String retaliationType, String description, String evidenceUrl) {
        this.worker = worker;
        this.protection = protection;
        this.retaliationType = retaliationType;
        this.description = description;
        this.evidenceUrl = evidenceUrl;
    }
}

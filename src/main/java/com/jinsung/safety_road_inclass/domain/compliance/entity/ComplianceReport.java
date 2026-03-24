package com.jinsung.safety_road_inclass.domain.compliance.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ComplianceReport - 컴플라이언스(안전관리 종합) 리포트 Entity
 *
 * 중대재해처벌법 핵심 증거자료:
 * - 제4조(안전보건관리체계 구축·이행) 의무 이행 종합 증거
 * - 월간/분기 단위로 안전 관리 현황을 자동 집계·저장
 * - DB에 영구 보존되어 고용노동부 점검 및 법적 분쟁 시 즉시 제출 가능
 *
 * 포함 항목:
 * - 안전 교육 이수 현황 (법정 교육시간 충족 여부)
 * - 위험성 평가 실시 기록 및 해결율
 * - 작업중지권 행사 이력
 * - 안전 점검 체크리스트 수행 이력
 * - 사고/아차사고 통계
 * - AI 기반 개선 권고사항
 * - 컴플라이언스 종합 점수 (0~100)
 */
@Entity
@Table(name = "compliance_reports", indexes = {
        @Index(name = "idx_cr_year_month", columnList = "report_year, report_month"),
        @Index(name = "idx_cr_generated_at", columnList = "created_at"),
        @Index(name = "idx_cr_generated_by", columnList = "generated_by")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ComplianceReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 리포트 대상 연도 */
    @Column(name = "report_year", nullable = false)
    private Integer reportYear;

    /** 리포트 대상 월 (1~12) */
    @Column(name = "report_month", nullable = false)
    private Integer reportMonth;

    // ─── 교육 이수 현황 ─────────────────────────────────────────────

    /** 교육 필수 건수 (해당 월) */
    @Column(nullable = false)
    private Integer educationRequired;

    /** 교육 완료 건수 */
    @Column(nullable = false)
    private Integer educationCompleted;

    /** 교육 이수율 (%) */
    @Column(nullable = false)
    private Integer educationRate;

    /** 누적 법정 교육시간 (시) */
    @Column(nullable = false)
    private Double cumulativeLegalHours;

    // ─── 위험성 평가 ─────────────────────────────────────────────────

    /** 위험성 평가 실시 횟수 */
    @Column(nullable = false)
    private Integer riskAssessmentConducted;

    /** 발견 사항 수 */
    @Column(nullable = false)
    private Integer riskFindings;

    /** 해결 건수 */
    @Column(nullable = false)
    private Integer riskResolved;

    /** 해결율 (%) */
    @Column(nullable = false)
    private Integer riskResolvedRate;

    // ─── 작업중지권 ─────────────────────────────────────────────────

    /** 작업중지권 행사 건수 */
    @Column(nullable = false)
    private Integer workStopExercised;

    // ─── 체크리스트 ─────────────────────────────────────────────────

    /** 의무 점검 건수 */
    @Column(nullable = false)
    private Integer checklistRequired;

    /** 실제 제출 건수 */
    @Column(nullable = false)
    private Integer checklistSubmitted;

    /** 제출율 (%) */
    @Column(nullable = false)
    private Integer checklistRate;

    // ─── 사고/아차사고 ─────────────────────────────────────────────

    /** 총 사고 건수 */
    @Column(nullable = false)
    private Integer incidentTotal;

    /** 사고 유형별 JSON ({"추락":2, "끼임":1, ...}) */
    @Column(columnDefinition = "TEXT")
    private String incidentByType;

    /** 사고 심각도별 JSON ({"high":1, "medium":3, "low":5}) */
    @Column(columnDefinition = "TEXT")
    private String incidentBySeverity;

    // ─── 종합 ─────────────────────────────────────────────────────

    /** 컴플라이언스 종합 점수 (0~100) */
    @Column(nullable = false)
    private Integer complianceScore;

    /** AI 개선 권고사항 JSON 배열 */
    @Column(columnDefinition = "TEXT")
    private String recommendations;

    /** 생성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by")
    private User generatedBy;

    @Builder
    public ComplianceReport(Integer reportYear, Integer reportMonth,
                             Integer educationRequired, Integer educationCompleted,
                             Integer educationRate, Double cumulativeLegalHours,
                             Integer riskAssessmentConducted, Integer riskFindings,
                             Integer riskResolved, Integer riskResolvedRate,
                             Integer workStopExercised,
                             Integer checklistRequired, Integer checklistSubmitted,
                             Integer checklistRate,
                             Integer incidentTotal, String incidentByType,
                             String incidentBySeverity,
                             Integer complianceScore, String recommendations,
                             User generatedBy) {
        this.reportYear = reportYear;
        this.reportMonth = reportMonth;
        this.educationRequired = educationRequired;
        this.educationCompleted = educationCompleted;
        this.educationRate = educationRate;
        this.cumulativeLegalHours = cumulativeLegalHours;
        this.riskAssessmentConducted = riskAssessmentConducted;
        this.riskFindings = riskFindings;
        this.riskResolved = riskResolved;
        this.riskResolvedRate = riskResolvedRate;
        this.workStopExercised = workStopExercised;
        this.checklistRequired = checklistRequired;
        this.checklistSubmitted = checklistSubmitted;
        this.checklistRate = checklistRate;
        this.incidentTotal = incidentTotal;
        this.incidentByType = incidentByType;
        this.incidentBySeverity = incidentBySeverity;
        this.complianceScore = complianceScore;
        this.recommendations = recommendations;
        this.generatedBy = generatedBy;
    }
}

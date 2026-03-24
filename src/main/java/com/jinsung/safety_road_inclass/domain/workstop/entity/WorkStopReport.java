package com.jinsung.safety_road_inclass.domain.workstop.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
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
 * WorkStopReport - 작업중지권 행사 기록 Entity
 *
 * 중대재해처벌법 증거자료 핵심 엔티티
 * - 산업안전보건법 제52조(근로자의 작업중지)
 * - 중대재해처벌법 제4조(안전보건관리체계 구축 의무) 이행 증거
 * - 동법 제5조(안전보건 교육) 관련 보호 기록
 *
 * 증거 보전: 모든 상태 변경은 StatusHistory로 추적되며, 삭제 불가(soft delete 미적용)
 */
@Entity
@Table(name = "work_stop_reports", indexes = {
        @Index(name = "idx_wsr_reporter", columnList = "reporter_id"),
        @Index(name = "idx_wsr_status", columnList = "status"),
        @Index(name = "idx_wsr_hazard_type", columnList = "hazard_type"),
        @Index(name = "idx_wsr_created_at", columnList = "created_at"),
        @Index(name = "idx_wsr_zone", columnList = "zone")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkStopReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 신고자 (User FK) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    /** 위험 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "hazard_type", nullable = false, length = 30)
    private HazardType hazardType;

    /** 위험 상세 설명 (증거 텍스트) */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /** 작업 구역 */
    @Column(length = 100)
    private String zone;

    /** 익명 신고 여부 */
    @Column(nullable = false)
    private Boolean isAnonymous = false;

    /** 현재 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.REPORTED;

    /** 증거 사진 URL */
    @Column(length = 500)
    private String photoUrl;

    /** 해결 시각 */
    private LocalDateTime resolvedAt;

    /** 해결자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolver_id")
    private User resolver;

    /** 해결 조치 내용 */
    @Column(columnDefinition = "TEXT")
    private String resolutionNote;

    /** 조치 계획 */
    @Column(columnDefinition = "TEXT")
    private String actionPlan;

    /** 조치 방법 */
    @Column(columnDefinition = "TEXT")
    private String actionMethod;

    /** 작업 재개 시각 */
    private LocalDateTime resumedAt;

    /** 작업 재개 확인 내용 */
    @Column(columnDefinition = "TEXT")
    private String resumeVerification;

    /** 부여 포인트 (용기 포인트) */
    @Column(nullable = false)
    private Integer rewardPoints = 500;

    /** 상태 변경 이력 (증거 보전 - 삭제 불가) */
    @OneToMany(mappedBy = "workStopReport", cascade = CascadeType.ALL, orphanRemoval = false)
    @OrderBy("changedAt ASC")
    private List<WorkStopStatusHistory> statusHistories = new ArrayList<>();

    /** 보호 기간 기록 */
    @OneToOne(mappedBy = "workStopReport", cascade = CascadeType.ALL)
    private WorkStopProtection protection;

    @Builder
    public WorkStopReport(User reporter, HazardType hazardType, String description,
                          String zone, Boolean isAnonymous, String photoUrl) {
        this.reporter = reporter;
        this.hazardType = hazardType;
        this.description = description;
        this.zone = zone;
        this.isAnonymous = isAnonymous != null ? isAnonymous : false;
        this.photoUrl = photoUrl;
        this.status = ReportStatus.REPORTED;
    }

    /** 초기 상태 이력 추가 */
    public void addInitialHistory() {
        WorkStopStatusHistory history = WorkStopStatusHistory.builder()
                .workStopReport(this)
                .status(ReportStatus.REPORTED)
                .changedBy(this.reporter)
                .build();
        this.statusHistories.add(history);
    }

    /** 상태 변경 (이력 자동 기록) */
    public void changeStatus(ReportStatus newStatus, User changedBy, String note) {
        this.status = newStatus;

        WorkStopStatusHistory history = WorkStopStatusHistory.builder()
                .workStopReport(this)
                .status(newStatus)
                .changedBy(changedBy)
                .note(note)
                .build();
        this.statusHistories.add(history);
    }

    /** 위험 해결 처리 */
    public void resolve(User resolver, String resolutionNote, String actionPlan, String actionMethod) {
        this.status = ReportStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.resolver = resolver;
        this.resolutionNote = resolutionNote;
        this.actionPlan = actionPlan;
        this.actionMethod = actionMethod;

        WorkStopStatusHistory history = WorkStopStatusHistory.builder()
                .workStopReport(this)
                .status(ReportStatus.RESOLVED)
                .changedBy(resolver)
                .note(resolutionNote)
                .build();
        this.statusHistories.add(history);
    }

    /** 작업 재개 처리 */
    public void resume(User confirmedBy, String resumeVerification) {
        this.status = ReportStatus.RESUMED;
        this.resumedAt = LocalDateTime.now();
        this.resumeVerification = resumeVerification;

        WorkStopStatusHistory history = WorkStopStatusHistory.builder()
                .workStopReport(this)
                .status(ReportStatus.RESUMED)
                .changedBy(confirmedBy)
                .note(resumeVerification)
                .build();
        this.statusHistories.add(history);
    }
}

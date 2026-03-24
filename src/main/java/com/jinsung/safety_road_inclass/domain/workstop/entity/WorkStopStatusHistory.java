package com.jinsung.safety_road_inclass.domain.workstop.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WorkStopStatusHistory - 작업중지 상태 변경 이력 Entity
 *
 * 중대재해 발생 시 증거자료 핵심:
 * - 신고 접수 → 조사 → 해결 → 재개까지 전 과정의 시간·담당자 추적
 * - 한번 기록된 이력은 수정·삭제 불가 (불변 감사 로그)
 * - 법적 분쟁 시 타임라인 증거로 활용
 */
@Entity
@Table(name = "work_stop_status_histories", indexes = {
        @Index(name = "idx_wssh_report_id", columnList = "report_id"),
        @Index(name = "idx_wssh_changed_at", columnList = "changed_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkStopStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private WorkStopReport workStopReport;

    /** 변경된 상태 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    /** 상태 변경 시각 (서버 시각 기준 — 조작 방지) */
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    /** 상태 변경자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    /** 변경 사유/메모 */
    @Column(columnDefinition = "TEXT")
    private String note;

    @Builder
    public WorkStopStatusHistory(WorkStopReport workStopReport, ReportStatus status,
                                  User changedBy, String note) {
        this.workStopReport = workStopReport;
        this.status = status;
        this.changedAt = LocalDateTime.now();
        this.changedBy = changedBy;
        this.note = note;
    }
}

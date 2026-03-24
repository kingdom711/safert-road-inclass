package com.jinsung.safety_road_inclass.domain.workstop.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WorkStopProtection - 작업중지권 행사자 보호 기간 Entity
 *
 * 산업안전보건법 제52조 제3항:
 * "사업주는 근로자가 작업중지를 한 것을 이유로 해고하거나
 *  그 밖의 불이익한 처우를 하여서는 아니 된다."
 *
 * 신고일로부터 30일간 보호 기간을 자동 설정하고,
 * 보호 기간 내 불이익 조치(보복) 발생 시 증거로 활용
 */
@Entity
@Table(name = "work_stop_protections", indexes = {
        @Index(name = "idx_wsp_worker_id", columnList = "worker_id"),
        @Index(name = "idx_wsp_end_date", columnList = "end_date")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkStopProtection extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 보호 대상 근로자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    /** 관련 작업중지 신고 */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false, unique = true)
    private WorkStopReport workStopReport;

    /** 보호 시작일 */
    @Column(nullable = false)
    private LocalDateTime startDate;

    /** 보호 종료일 (기본 30일) */
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    /** 보호 활성 여부 */
    @Column(nullable = false)
    private Boolean isActive = true;

    @Builder
    public WorkStopProtection(User worker, WorkStopReport workStopReport) {
        this.worker = worker;
        this.workStopReport = workStopReport;
        this.startDate = LocalDateTime.now();
        this.endDate = this.startDate.plusDays(30);
        this.isActive = true;
    }

    /** 보호 기간 활성 여부 확인 */
    public boolean isCurrentlyActive() {
        return this.isActive && LocalDateTime.now().isBefore(this.endDate);
    }
}

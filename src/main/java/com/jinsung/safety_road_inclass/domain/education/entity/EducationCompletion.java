package com.jinsung.safety_road_inclass.domain.education.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * EducationCompletion - 교육 수료 완료 기록 Entity
 *
 * 목적: 고용노동부 사후 점검 대비 교육 이수 증거 보존
 * - 시청 시간, 퀴즈 결과, 접속 환경을 함께 저장
 * - SHA-256 해시 체이닝으로 레코드 위변조 탐지
 * - 수료증 번호로 제3자 검증 지원
 */
@Entity
@Table(name = "education_completions", indexes = {
        @Index(name = "idx_edu_completion_user_id",  columnList = "user_id"),
        @Index(name = "idx_edu_completion_edu_id",   columnList = "education_id"),
        @Index(name = "idx_edu_completion_cert",     columnList = "cert_number"),
        @Index(name = "idx_edu_completion_user_edu", columnList = "user_id, education_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EducationCompletion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ─── 교육 식별 정보 ─────────────────────────────────────────────
    @Column(name = "education_id", nullable = false, length = 100)
    private String educationId;

    @Column(name = "education_title", nullable = false, length = 255)
    private String educationTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_type", nullable = false, length = 20)
    private EducationType educationType;

    // ─── 시청 시간 기록 ─────────────────────────────────────────────
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(name = "video_watch_seconds", nullable = false)
    private int videoWatchSeconds;

    @Column(name = "video_total_seconds", nullable = false)
    private int videoTotalSeconds;

    @Column(name = "video_watch_ratio", nullable = false, precision = 5, scale = 4)
    private BigDecimal videoWatchRatio;

    // ─── 퀴즈 결과 ──────────────────────────────────────────────────
    @Column(name = "quiz_score", nullable = false)
    private int quizScore;

    @Column(name = "quiz_passed", nullable = false)
    private boolean quizPassed;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    // ─── 접속 환경 ──────────────────────────────────────────────────
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "device_hash", length = 64)
    private String deviceHash;

    // ─── 위변조 방지 ────────────────────────────────────────────────
    @Column(name = "cert_number", length = 100, unique = true)
    private String certNumber;

    @Column(name = "integrity_hash", length = 64)
    private String integrityHash;

    @Column(name = "prev_hash", length = 64)
    private String prevHash;

    @Column(name = "tsa_token", columnDefinition = "TEXT")
    private String tsaToken;

    @Builder
    public EducationCompletion(User user, String educationId, String educationTitle,
                               EducationType educationType, LocalDateTime startedAt,
                               LocalDateTime completedAt, int videoWatchSeconds,
                               int videoTotalSeconds, BigDecimal videoWatchRatio,
                               int quizScore, boolean quizPassed, int attemptCount,
                               String ipAddress, String userAgent, String deviceHash) {
        this.user = user;
        this.educationId = educationId;
        this.educationTitle = educationTitle;
        this.educationType = educationType;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.videoWatchSeconds = videoWatchSeconds;
        this.videoTotalSeconds = videoTotalSeconds;
        this.videoWatchRatio = videoWatchRatio;
        this.quizScore = quizScore;
        this.quizPassed = quizPassed;
        this.attemptCount = attemptCount;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceHash = deviceHash;
    }

    /**
     * 저장 후 해시 체인 정보 업데이트
     * (서비스 레이어에서 ID 확정 후 호출)
     */
    public void applyHashChain(String certNumber, String integrityHash, String prevHash) {
        this.certNumber = certNumber;
        this.integrityHash = integrityHash;
        this.prevHash = prevHash;
    }
}

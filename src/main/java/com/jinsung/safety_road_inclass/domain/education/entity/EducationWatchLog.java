package com.jinsung.safety_road_inclass.domain.education.entity;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * EducationWatchLog - 영상 시청 체크포인트 기록 Entity
 *
 * 목적: "실제로 영상을 시청했음"을 증명하는 서버 측 타임스탬프 기록
 * - 프론트엔드가 30초 간격으로 현재 재생 위치를 전송
 * - 서버 시각(server_time)으로 기록 → 클라이언트 시각 조작 불가
 * - 탭 비활성화(포커스 아웃) 상태도 함께 기록
 */
@Entity
@Table(name = "education_watch_logs", indexes = {
        @Index(name = "idx_watch_log_user_edu",    columnList = "user_id, education_id"),
        @Index(name = "idx_watch_log_completion",  columnList = "completion_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EducationWatchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 완료 후 completion_id를 역참조로 업데이트 (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completion_id")
    private EducationCompletion completion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "education_id", nullable = false, length = 100)
    private String educationId;

    // ─── 클라이언트 보고값 ───────────────────────────────────────────
    @Column(name = "checkpoint_sec", nullable = false)
    private int checkpointSec;

    @Column(name = "total_sec", nullable = false)
    private int totalSec;

    @Column(name = "is_playing", nullable = false)
    private boolean isPlaying;

    @Column(name = "tab_visible", nullable = false)
    private boolean tabVisible;

    // ─── 서버 기록 시각 (위변조 방지 핵심) ──────────────────────────
    @CreationTimestamp
    @Column(name = "server_time", nullable = false, updatable = false)
    private LocalDateTime serverTime;

    @Builder
    public EducationWatchLog(EducationCompletion completion, User user, String educationId,
                             int checkpointSec, int totalSec, boolean isPlaying, boolean tabVisible) {
        this.completion = completion;
        this.user = user;
        this.educationId = educationId;
        this.checkpointSec = checkpointSec;
        this.totalSec = totalSec;
        this.isPlaying = isPlaying;
        this.tabVisible = tabVisible;
    }
}

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
 * QuizAttemptLog - 퀴즈 개별 답안 이력 Entity
 *
 * 목적: "실제로 퀴즈를 직접 풀었음"을 증명
 * - 문항별 선택지, 정오, 소요시간을 모두 저장
 * - 재시도(attempt_number)별 전체 이력 보존
 * - 너무 빠른 제출(시간이 0초)은 서비스에서 검증 가능
 */
@Entity
@Table(name = "quiz_attempt_logs", indexes = {
        @Index(name = "idx_quiz_log_user_edu",   columnList = "user_id, education_id"),
        @Index(name = "idx_quiz_log_completion", columnList = "completion_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizAttemptLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completion_id")
    private EducationCompletion completion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "education_id", nullable = false, length = 100)
    private String educationId;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    // ─── 문항별 답안 ─────────────────────────────────────────────────
    @Column(name = "question_id", nullable = false, length = 100)
    private String questionId;

    @Column(name = "selected_option", nullable = false)
    private int selectedOption;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "time_spent_sec")
    private Integer timeSpentSec;

    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Builder
    public QuizAttemptLog(EducationCompletion completion, User user, String educationId,
                          int attemptNumber, String questionId, int selectedOption,
                          boolean correct, Integer timeSpentSec) {
        this.completion = completion;
        this.user = user;
        this.educationId = educationId;
        this.attemptNumber = attemptNumber;
        this.questionId = questionId;
        this.selectedOption = selectedOption;
        this.correct = correct;
        this.timeSpentSec = timeSpentSec;
    }
}

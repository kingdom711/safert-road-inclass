package com.jinsung.safety_road_inclass.domain.education.repository;

import com.jinsung.safety_road_inclass.domain.education.entity.QuizAttemptLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * QuizAttemptLogRepository
 */
public interface QuizAttemptLogRepository extends JpaRepository<QuizAttemptLog, Long> {

    /**
     * 특정 유저 + 교육ID의 전체 퀴즈 시도 이력 조회
     */
    List<QuizAttemptLog> findByUserIdAndEducationIdOrderBySubmittedAtAsc(Long userId, String educationId);

    /**
     * 특정 completion의 퀴즈 답안 조회
     */
    List<QuizAttemptLog> findByCompletionIdOrderBySubmittedAtAsc(Long completionId);

    /**
     * 특정 시도 번호의 답안만 조회
     */
    List<QuizAttemptLog> findByUserIdAndEducationIdAndAttemptNumberOrderBySubmittedAtAsc(
            Long userId, String educationId, int attemptNumber);
}

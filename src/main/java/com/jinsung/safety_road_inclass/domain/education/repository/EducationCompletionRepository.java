package com.jinsung.safety_road_inclass.domain.education.repository;

import com.jinsung.safety_road_inclass.domain.education.entity.EducationCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * EducationCompletionRepository
 */
public interface EducationCompletionRepository extends JpaRepository<EducationCompletion, Long> {

    /**
     * 특정 유저의 가장 최근 수료 기록 조회 (해시 체이닝용)
     */
    Optional<EducationCompletion> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 수료증 번호로 조회 (제3자 검증용)
     */
    Optional<EducationCompletion> findByCertNumber(String certNumber);

    /**
     * 특정 유저 + 교육ID 수료 이력 조회
     */
    List<EducationCompletion> findByUserIdAndEducationIdOrderByCreatedAtDesc(Long userId, String educationId);

    /**
     * 특정 유저의 전체 수료 이력 조회
     */
    List<EducationCompletion> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 특정 유저가 해당 교육을 합격했는지 확인
     */
    boolean existsByUserIdAndEducationIdAndQuizPassed(Long userId, String educationId, boolean quizPassed);

    /**
     * 유저의 수료 시도 횟수 (중복 이수 방지 참고용)
     */
    @Query("SELECT COUNT(e) FROM EducationCompletion e WHERE e.user.id = :userId AND e.educationId = :educationId")
    int countAttemptsByUserIdAndEducationId(@Param("userId") Long userId, @Param("educationId") String educationId);

    // ─── 컴플라이언스 리포트 / 증거자료 조회용 ──────────────────────

    /** 기간별 전체 이수 기록 조회 */
    @Query("SELECT e FROM EducationCompletion e WHERE e.completedAt BETWEEN :start AND :end ORDER BY e.completedAt DESC")
    List<EducationCompletion> findByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 기간별 이수 건수 */
    long countByCompletedAtBetween(LocalDateTime start, LocalDateTime end);

    /** 기간별 합격 건수 */
    long countByCompletedAtBetweenAndQuizPassed(LocalDateTime start, LocalDateTime end, boolean quizPassed);

    /** 기간별 고유 이수자 수 */
    @Query("SELECT COUNT(DISTINCT e.user.id) FROM EducationCompletion e WHERE e.completedAt BETWEEN :start AND :end AND e.quizPassed = true")
    long countDistinctPassedUsersByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 부서 대항전용: 주어진 유저 목록 중 기간 내 합격자(unique) 수 */
    @Query("SELECT COUNT(DISTINCT e.user.id) FROM EducationCompletion e " +
            "WHERE e.user.id IN :userIds AND e.quizPassed = true " +
            "AND e.completedAt BETWEEN :start AND :end")
    long countDistinctPassedUsersByUserIdsAndPeriod(@Param("userIds") java.util.Collection<Long> userIds,
                                                    @Param("start") LocalDateTime start,
                                                    @Param("end") LocalDateTime end);
}

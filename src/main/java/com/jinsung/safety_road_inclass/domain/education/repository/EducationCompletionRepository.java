package com.jinsung.safety_road_inclass.domain.education.repository;

import com.jinsung.safety_road_inclass.domain.education.entity.EducationCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}

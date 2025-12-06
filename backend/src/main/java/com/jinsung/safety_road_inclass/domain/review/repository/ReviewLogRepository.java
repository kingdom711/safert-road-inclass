package com.jinsung.safety_road_inclass.domain.review.repository;

import com.jinsung.safety_road_inclass.domain.review.entity.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ReviewLogRepository - 검토 로그 데이터 접근
 */
public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {

    /**
     * 체크리스트별 검토 이력 조회
     */
    List<ReviewLog> findByChecklistIdOrderByReviewedAtDesc(Long checklistId);

    /**
     * 검토자별 검토 이력
     */
    List<ReviewLog> findByReviewerIdOrderByReviewedAtDesc(Long reviewerId);

    /**
     * 최근 검토 이력 조회 (N개)
     */
    @Query("SELECT rl FROM ReviewLog rl " +
           "JOIN FETCH rl.checklist c " +
           "JOIN FETCH rl.reviewer " +
           "ORDER BY rl.reviewedAt DESC")
    List<ReviewLog> findRecentReviews();
}


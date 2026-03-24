package com.jinsung.safety_road_inclass.domain.risk.repository;

import com.jinsung.safety_road_inclass.domain.risk.entity.Countermeasure;
import com.jinsung.safety_road_inclass.domain.risk.entity.CountermeasureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * CountermeasureRepository - 개선 대책 데이터 접근
 */
public interface CountermeasureRepository extends JpaRepository<Countermeasure, Long> {

    /**
     * 위험 평가별 대책 조회
     */
    List<Countermeasure> findByRiskAssessmentId(Long riskAssessmentId);

    /**
     * 상태별 대책 조회
     */
    List<Countermeasure> findByStatusOrderByDueDateAsc(CountermeasureStatus status);

    /**
     * 미완료 대책 조회 (PLANNED, IN_PROGRESS)
     */
    @Query("SELECT c FROM Countermeasure c " +
           "WHERE c.status != 'COMPLETED' " +
           "ORDER BY c.dueDate ASC")
    List<Countermeasure> findIncomplete();

    /**
     * 기한 초과 미완료 대책
     */
    @Query("SELECT c FROM Countermeasure c " +
           "WHERE c.status != 'COMPLETED' AND c.dueDate < :today " +
           "ORDER BY c.dueDate ASC")
    List<Countermeasure> findOverdue(@Param("today") LocalDate today);

    /**
     * 상태별 개수
     */
    @Query("SELECT c.status, COUNT(c) FROM Countermeasure c " +
           "GROUP BY c.status")
    List<Object[]> countByStatus();

    // ─── 컴플라이언스 리포트용 ──────────────────────────────────────

    /** 기간별 완료된 대책 건수 */
    @Query("SELECT COUNT(c) FROM Countermeasure c WHERE c.status = 'COMPLETED' AND c.createdAt BETWEEN :start AND :end")
    long countCompletedByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}


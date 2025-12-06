package com.jinsung.safety_road_inclass.domain.checklist.repository;

import com.jinsung.safety_road_inclass.domain.checklist.entity.Checklist;
import com.jinsung.safety_road_inclass.domain.checklist.entity.ChecklistStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ChecklistRepository - 체크리스트 데이터 접근
 */
public interface ChecklistRepository extends JpaRepository<Checklist, Long> {

    /**
     * 내 체크리스트 목록 (페이징)
     */
    Page<Checklist> findByCreatedByIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 상태별 조회
     */
    List<Checklist> findByStatusOrderBySubmittedAtDesc(ChecklistStatus status);

    /**
     * 위험 항목이 있는 체크리스트 조회
     */
    @Query("SELECT DISTINCT c FROM Checklist c " +
           "JOIN c.items i " +
           "WHERE i.riskFlag = true AND c.status = :status")
    List<Checklist> findWithRiskItems(@Param("status") ChecklistStatus status);

    /**
     * 상세 조회 (N+1 방지)
     */
    @Query("SELECT c FROM Checklist c " +
           "JOIN FETCH c.template t " +
           "JOIN FETCH c.createdBy " +
           "LEFT JOIN FETCH c.items " +
           "WHERE c.id = :id")
    Optional<Checklist> findByIdWithDetails(@Param("id") Long id);

    /**
     * 기간별 통계
     */
    @Query("SELECT c.status, COUNT(c) FROM Checklist c " +
           "WHERE c.workDate BETWEEN :startDate AND :endDate " +
           "GROUP BY c.status")
    List<Object[]> countByStatusAndDateRange(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}


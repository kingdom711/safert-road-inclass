package com.jinsung.safety_road_inclass.domain.risk.repository;

import com.jinsung.safety_road_inclass.domain.risk.entity.RiskAssessment;
import com.jinsung.safety_road_inclass.domain.risk.entity.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * RiskAssessmentRepository - 위험성 평가 데이터 접근
 */
public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, Long> {

    /**
     * 체크리스트 항목별 평가 조회
     */
    Optional<RiskAssessment> findByChecklistItemId(Long checklistItemId);

    /**
     * 체크리스트 항목별 평가 존재 여부
     */
    boolean existsByChecklistItemId(Long checklistItemId);

    /**
     * 위험 레벨별 조회
     */
    List<RiskAssessment> findByRiskLevelOrderByAssessedAtDesc(RiskLevel riskLevel);

    /**
     * 고위험 항목 조회 (HIGH, CRITICAL)
     */
    @Query("SELECT ra FROM RiskAssessment ra " +
           "WHERE ra.riskLevel IN ('HIGH', 'CRITICAL') " +
           "ORDER BY ra.riskScore DESC, ra.assessedAt DESC")
    List<RiskAssessment> findHighRiskItems();

    /**
     * 대시보드 통계: 위험 레벨별 개수
     */
    @Query("SELECT ra.riskLevel, COUNT(ra) FROM RiskAssessment ra " +
           "GROUP BY ra.riskLevel")
    List<Object[]> countByRiskLevel();

    /**
     * 상세 조회 (N+1 방지)
     */
    @Query("SELECT ra FROM RiskAssessment ra " +
           "JOIN FETCH ra.checklistItem ci " +
           "JOIN FETCH ci.checklist c " +
           "JOIN FETCH ra.assessedBy " +
           "LEFT JOIN FETCH ra.countermeasures " +
           "WHERE ra.id = :id")
    Optional<RiskAssessment> findByIdWithDetails(@Param("id") Long id);
}


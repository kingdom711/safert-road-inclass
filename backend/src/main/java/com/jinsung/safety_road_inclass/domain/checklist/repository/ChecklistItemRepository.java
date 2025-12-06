package com.jinsung.safety_road_inclass.domain.checklist.repository;

import com.jinsung.safety_road_inclass.domain.checklist.entity.ChecklistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * ChecklistItemRepository - 체크리스트 항목 데이터 접근
 */
public interface ChecklistItemRepository extends JpaRepository<ChecklistItem, Long> {

    /**
     * 체크리스트별 항목 조회
     */
    List<ChecklistItem> findByChecklistId(Long checklistId);

    /**
     * 위험 항목 조회 (riskFlag = true)
     */
    List<ChecklistItem> findByChecklistIdAndRiskFlagTrue(Long checklistId);

    /**
     * 미평가 위험 항목 조회
     */
    @Query("SELECT ci FROM ChecklistItem ci " +
           "WHERE ci.riskFlag = true " +
           "AND NOT EXISTS (SELECT 1 FROM RiskAssessment ra WHERE ra.checklistItem.id = ci.id)")
    List<ChecklistItem> findByRiskFlagTrueAndRiskAssessmentIsNull();
}


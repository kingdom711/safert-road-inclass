package com.jinsung.safety_road_inclass.domain.template.repository;

import com.jinsung.safety_road_inclass.domain.template.entity.ChecklistTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ChecklistTemplateRepository - 체크리스트 템플릿 데이터 접근
 */
public interface ChecklistTemplateRepository extends JpaRepository<ChecklistTemplate, Long> {

    /**
     * 활성화된 템플릿 목록 조회
     */
    List<ChecklistTemplate> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * 작업 유형별 활성화된 템플릿 조회
     */
    List<ChecklistTemplate> findByWorkTypeIdAndIsActiveTrueOrderByCreatedAtDesc(Long workTypeId);

    /**
     * 템플릿 상세 조회 (N+1 방지)
     */
    @Query("SELECT t FROM ChecklistTemplate t " +
           "JOIN FETCH t.workType " +
           "LEFT JOIN FETCH t.items " +
           "WHERE t.id = :id")
    Optional<ChecklistTemplate> findByIdWithItems(@Param("id") Long id);

    /**
     * 작업 유형별 템플릿 개수
     */
    long countByWorkTypeId(Long workTypeId);
}


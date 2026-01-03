package com.jinsung.safety_road_inclass.domain.template.repository;

import com.jinsung.safety_road_inclass.domain.template.entity.TemplateItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * TemplateItemRepository - 템플릿 항목 데이터 접근
 */
public interface TemplateItemRepository extends JpaRepository<TemplateItem, Long> {

    /**
     * 템플릿별 항목 조회 (순서대로)
     */
    List<TemplateItem> findByTemplateIdOrderByItemOrderAsc(Long templateId);

    /**
     * 템플릿별 항목 개수
     */
    int countByTemplateId(Long templateId);

    /**
     * 템플릿 삭제 시 항목도 함께 삭제 (CASCADE)
     */
    void deleteByTemplateId(Long templateId);
}


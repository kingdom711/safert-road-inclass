package com.jinsung.safety_road_inclass.domain.template.service;

import com.jinsung.safety_road_inclass.domain.template.dto.TemplateDetailResponse;
import com.jinsung.safety_road_inclass.domain.template.dto.TemplateListResponse;
import com.jinsung.safety_road_inclass.domain.template.entity.ChecklistTemplate;
import com.jinsung.safety_road_inclass.domain.template.repository.ChecklistTemplateRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TemplateService - 템플릿 서비스
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TemplateService {

    private final ChecklistTemplateRepository templateRepository;

    /**
     * 템플릿 목록 조회
     */
    public List<TemplateListResponse> getTemplates(Long workTypeId) {
        List<ChecklistTemplate> templates;
        
        if (workTypeId != null) {
            templates = templateRepository.findByWorkTypeIdAndIsActiveTrueOrderByCreatedAtDesc(workTypeId);
            log.debug("작업 유형별 템플릿 조회: workTypeId={}, count={}", workTypeId, templates.size());
        } else {
            templates = templateRepository.findByIsActiveTrueOrderByCreatedAtDesc();
            log.debug("전체 템플릿 조회: count={}", templates.size());
        }
        
        return templates.stream()
            .map(TemplateListResponse::from)
            .toList();
    }

    /**
     * 템플릿 상세 조회
     */
    public TemplateDetailResponse getTemplateDetail(Long templateId) {
        ChecklistTemplate template = templateRepository.findByIdWithItems(templateId)
            .orElseThrow(() -> new CustomException(ErrorCode.TEMPLATE_NOT_FOUND));
        
        log.info("템플릿 상세 조회: templateId={}, itemCount={}", 
                 templateId, template.getItems().size());
        
        return TemplateDetailResponse.from(template);
    }
}


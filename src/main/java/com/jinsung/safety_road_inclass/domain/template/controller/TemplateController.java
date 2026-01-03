package com.jinsung.safety_road_inclass.domain.template.controller;

import com.jinsung.safety_road_inclass.domain.template.dto.TemplateDetailResponse;
import com.jinsung.safety_road_inclass.domain.template.dto.TemplateListResponse;
import com.jinsung.safety_road_inclass.domain.template.service.TemplateService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TemplateController - 템플릿 API
 */
@Tag(name = "Template", description = "체크리스트 템플릿 API")
@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @Operation(
        summary = "템플릿 목록 조회", 
        description = "활성화된 체크리스트 템플릿 목록을 조회합니다. workTypeId로 필터링 가능합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<TemplateListResponse>>> getTemplates(
            @Parameter(description = "작업 유형 ID (옵션)", example = "1")
            @RequestParam(required = false) Long workTypeId) {
        
        List<TemplateListResponse> templates = templateService.getTemplates(workTypeId);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @Operation(
        summary = "템플릿 상세 조회", 
        description = "특정 템플릿의 상세 정보와 모든 점검 항목을 조회합니다."
    )
    @GetMapping("/{templateId}")
    public ResponseEntity<ApiResponse<TemplateDetailResponse>> getTemplateDetail(
            @Parameter(description = "템플릿 ID", example = "1")
            @PathVariable Long templateId) {
        
        TemplateDetailResponse template = templateService.getTemplateDetail(templateId);
        return ResponseEntity.ok(ApiResponse.success(template));
    }
}


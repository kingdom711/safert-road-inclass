package com.jinsung.safety_road_inclass.domain.template.dto;

import com.jinsung.safety_road_inclass.domain.template.entity.ChecklistTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * TemplateListResponse - 템플릿 목록 응답 DTO
 */
@Schema(description = "템플릿 목록 응답")
@Getter
@Builder
public class TemplateListResponse {

    @Schema(description = "템플릿 ID", example = "1")
    private Long id;

    @Schema(description = "작업 유형 ID", example = "1")
    private Long workTypeId;

    @Schema(description = "작업 유형명", example = "사다리 작업")
    private String workTypeName;

    @Schema(description = "템플릿 제목", example = "사다리 안전점검표")
    private String title;

    @Schema(description = "템플릿 설명")
    private String description;

    @Schema(description = "활성화 여부", example = "true")
    private Boolean isActive;

    @Schema(description = "버전", example = "1")
    private Integer version;

    @Schema(description = "항목 개수", example = "10")
    private Integer itemCount;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    public static TemplateListResponse from(ChecklistTemplate template) {
        return TemplateListResponse.builder()
            .id(template.getId())
            .workTypeId(template.getWorkType().getId())
            .workTypeName(template.getWorkType().getName())
            .title(template.getTitle())
            .description(template.getDescription())
            .isActive(template.getIsActive())
            .version(template.getVersion())
            .itemCount(template.getItems().size())
            .createdAt(template.getCreatedAt())
            .build();
    }
}


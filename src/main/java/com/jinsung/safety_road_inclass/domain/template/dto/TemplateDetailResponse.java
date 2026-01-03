package com.jinsung.safety_road_inclass.domain.template.dto;

import com.jinsung.safety_road_inclass.domain.template.entity.ChecklistTemplate;
import com.jinsung.safety_road_inclass.domain.template.entity.TemplateItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * TemplateDetailResponse - 템플릿 상세 응답 DTO
 */
@Schema(description = "템플릿 상세 응답")
@Getter
@Builder
public class TemplateDetailResponse {

    @Schema(description = "템플릿 ID")
    private Long id;

    @Schema(description = "작업 유형")
    private WorkTypeInfo workType;

    @Schema(description = "템플릿 제목")
    private String title;

    @Schema(description = "템플릿 설명")
    private String description;

    @Schema(description = "활성화 여부")
    private Boolean isActive;

    @Schema(description = "버전")
    private Integer version;

    @Schema(description = "항목 목록")
    private List<TemplateItemInfo> items;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    public static TemplateDetailResponse from(ChecklistTemplate template) {
        return TemplateDetailResponse.builder()
            .id(template.getId())
            .workType(WorkTypeInfo.from(template.getWorkType()))
            .title(template.getTitle())
            .description(template.getDescription())
            .isActive(template.getIsActive())
            .version(template.getVersion())
            .items(template.getItems().stream()
                .map(TemplateItemInfo::from)
                .toList())
            .createdAt(template.getCreatedAt())
            .build();
    }

    @Schema(description = "작업 유형 정보")
    @Getter
    @Builder
    public static class WorkTypeInfo {
        @Schema(description = "작업 유형 ID")
        private Long id;

        @Schema(description = "작업 유형명")
        private String name;

        @Schema(description = "작업 유형 설명")
        private String description;

        public static WorkTypeInfo from(com.jinsung.safety_road_inclass.domain.template.entity.WorkType workType) {
            return WorkTypeInfo.builder()
                .id(workType.getId())
                .name(workType.getName())
                .description(workType.getDescription())
                .build();
        }
    }

    @Schema(description = "템플릿 항목 정보")
    @Getter
    @Builder
    public static class TemplateItemInfo {
        @Schema(description = "항목 ID")
        private Long id;

        @Schema(description = "항목 순서")
        private Integer itemOrder;

        @Schema(description = "점검 내용")
        private String content;

        @Schema(description = "필수 여부")
        private Boolean isRequired;

        @Schema(description = "카테고리")
        private String category;

        public static TemplateItemInfo from(TemplateItem item) {
            return TemplateItemInfo.builder()
                .id(item.getId())
                .itemOrder(item.getItemOrder())
                .content(item.getContent())
                .isRequired(item.getIsRequired())
                .category(item.getCategory())
                .build();
        }
    }
}


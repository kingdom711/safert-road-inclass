package com.jinsung.safety_road_inclass.domain.checklist.dto;

import com.jinsung.safety_road_inclass.domain.checklist.entity.Answer;
import com.jinsung.safety_road_inclass.domain.checklist.entity.ChecklistItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * ChecklistItemResponse - 체크리스트 항목 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "체크리스트 항목 응답")
public class ChecklistItemResponse {

    @Schema(description = "항목 ID", example = "1")
    private Long id;

    @Schema(description = "템플릿 항목 ID", example = "1")
    private Long templateItemId;

    @Schema(description = "항목 내용", example = "안전모를 착용하였는가?")
    private String content;

    @Schema(description = "응답 (YES/NO/NA)", example = "YES")
    private Answer answer;

    @Schema(description = "코멘트", example = "문제 없음")
    private String comment;

    @Schema(description = "위험 플래그 (NO인 경우 true)", example = "false")
    private Boolean riskFlag;

    public static ChecklistItemResponse from(ChecklistItem item) {
        return ChecklistItemResponse.builder()
                .id(item.getId())
                .templateItemId(item.getTemplateItem().getId())
                .content(item.getTemplateItem().getContent())
                .answer(item.getAnswer())
                .comment(item.getComment())
                .riskFlag(item.getRiskFlag())
                .build();
    }
}


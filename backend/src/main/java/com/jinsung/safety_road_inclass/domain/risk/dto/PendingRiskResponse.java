package com.jinsung.safety_road_inclass.domain.risk.dto;

import com.jinsung.safety_road_inclass.domain.checklist.entity.ChecklistItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * PendingRiskResponse - 평가 대기 중인 위험 항목 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "평가 대기 중인 위험 항목")
public class PendingRiskResponse {

    @Schema(description = "체크리스트 항목 ID", example = "5")
    private Long checklistItemId;

    @Schema(description = "체크리스트 ID", example = "1")
    private Long checklistId;

    @Schema(description = "항목 내용", example = "안전모를 착용하였는가?")
    private String itemContent;

    @Schema(description = "현장명", example = "서울 강남구 테헤란로 현장")
    private String siteName;

    @Schema(description = "작업자 이름", example = "김작업")
    private String workerName;

    @Schema(description = "코멘트", example = "안전모 미착용 발견")
    private String comment;

    public static PendingRiskResponse from(ChecklistItem item) {
        return PendingRiskResponse.builder()
                .checklistItemId(item.getId())
                .checklistId(item.getChecklist().getId())
                .itemContent(item.getTemplateItem().getContent())
                .siteName(item.getChecklist().getSiteName())
                .workerName(item.getChecklist().getCreatedBy().getName())
                .comment(item.getComment())
                .build();
    }
}


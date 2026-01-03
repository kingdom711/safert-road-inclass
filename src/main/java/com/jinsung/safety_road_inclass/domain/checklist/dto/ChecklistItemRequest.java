package com.jinsung.safety_road_inclass.domain.checklist.dto;

import com.jinsung.safety_road_inclass.domain.checklist.entity.Answer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ChecklistItemRequest - 체크리스트 항목 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "체크리스트 항목 요청")
public class ChecklistItemRequest {

    @NotNull(message = "템플릿 항목 ID는 필수입니다.")
    @Schema(description = "템플릿 항목 ID", example = "1")
    private Long templateItemId;

    @NotNull(message = "응답은 필수입니다.")
    @Schema(description = "응답 (YES/NO/NA)", example = "YES")
    private Answer answer;

    @Schema(description = "코멘트 (선택사항)", example = "문제 없음")
    private String comment;
}


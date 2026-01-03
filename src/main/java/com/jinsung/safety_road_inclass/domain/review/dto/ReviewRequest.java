package com.jinsung.safety_road_inclass.domain.review.dto;

import com.jinsung.safety_road_inclass.domain.review.entity.ReviewAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ReviewRequest - 검토 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "검토 요청")
public class ReviewRequest {

    @NotNull(message = "검토 액션은 필수입니다.")
    @Schema(description = "검토 액션 (APPROVE/REJECT)", example = "APPROVE")
    private ReviewAction action;

    @Size(max = 1000, message = "검토 의견은 1000자 이하여야 합니다.")
    @Schema(description = "검토 의견", example = "검토 완료, 문제 없음")
    private String comment;
}


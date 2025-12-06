package com.jinsung.safety_road_inclass.domain.risk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * RiskAssessRequest - 위험성 평가 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "위험성 평가 요청")
public class RiskAssessRequest {

    @NotNull(message = "빈도는 필수입니다.")
    @Min(value = 1, message = "빈도는 1 이상이어야 합니다.")
    @Max(value = 5, message = "빈도는 5 이하여야 합니다.")
    @Schema(description = "빈도 (1-5)", example = "3")
    private Integer frequency;

    @NotNull(message = "강도는 필수입니다.")
    @Min(value = 1, message = "강도는 1 이상이어야 합니다.")
    @Max(value = 5, message = "강도는 5 이하여야 합니다.")
    @Schema(description = "강도 (1-5)", example = "4")
    private Integer severity;

    @Size(max = 1000, message = "설명은 1000자 이하여야 합니다.")
    @Schema(description = "위험 상황 설명", example = "추락 위험이 높은 작업 환경")
    private String description;

    @Valid
    @Schema(description = "개선 대책 목록")
    private List<CountermeasureRequest> countermeasures;
}


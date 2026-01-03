package com.jinsung.safety_road_inclass.domain.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AI 분석 요청 DTO
 * - 체크리스트 내용을 기반으로 위험 요인 분석
 * 
 * @apiNote POST /api/v1/ai/analyze 엔드포인트에서 사용
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 텍스트 분석 요청")
public class AiAnalysisRequest {
    
    @NotNull(message = "체크리스트 ID는 필수입니다.")
    @Schema(
        description = "체크리스트 ID", 
        example = "1", 
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long checklistId;
    
    @NotBlank(message = "분석할 내용은 필수입니다.")
    @Size(max = 5000, message = "분석 내용은 5000자 이하여야 합니다.")
    @Schema(
        description = "분석할 체크리스트 내용 텍스트", 
        example = "안전모 착용 여부: 미착용, 안전난간 상태: 불안정, 작업 높이: 2층", 
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 5000
    )
    private String content;
}



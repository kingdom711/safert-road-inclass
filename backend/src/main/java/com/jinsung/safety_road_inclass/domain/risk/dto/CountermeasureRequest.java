package com.jinsung.safety_road_inclass.domain.risk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * CountermeasureRequest - 개선 대책 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "개선 대책 요청")
public class CountermeasureRequest {

    @NotNull(message = "대책 내용은 필수입니다.")
    @Size(max = 1000, message = "대책 내용은 1000자 이하여야 합니다.")
    @Schema(description = "대책 내용", example = "안전 난간 설치")
    private String content;

    @Schema(description = "완료 예정일", example = "2025-12-20")
    private LocalDate dueDate;
}


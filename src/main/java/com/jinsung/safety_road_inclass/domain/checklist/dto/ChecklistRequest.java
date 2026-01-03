package com.jinsung.safety_road_inclass.domain.checklist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * ChecklistRequest - 체크리스트 제출 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "체크리스트 제출 요청")
public class ChecklistRequest {

    @NotNull(message = "템플릿 ID는 필수입니다.")
    @Schema(description = "체크리스트 템플릿 ID", example = "1")
    private Long templateId;

    @NotBlank(message = "현장명은 필수입니다.")
    @Size(max = 100, message = "현장명은 100자 이하여야 합니다.")
    @Schema(description = "현장명", example = "서울 강남구 테헤란로 현장")
    private String siteName;

    @NotNull(message = "작업일은 필수입니다.")
    @Schema(description = "작업일", example = "2025-12-06")
    private LocalDate workDate;

    @Size(max = 500, message = "비고는 500자 이하여야 합니다.")
    @Schema(description = "비고 (선택사항)", example = "특이사항 없음")
    private String remarks;

    @NotNull(message = "항목 목록은 필수입니다.")
    @Size(min = 1, message = "최소 1개 이상의 항목이 필요합니다.")
    @Valid
    @Schema(description = "체크리스트 항목 목록")
    private List<ChecklistItemRequest> items;
}


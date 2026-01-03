package com.jinsung.safety_road_inclass.domain.risk.dto;

import com.jinsung.safety_road_inclass.domain.risk.entity.Countermeasure;
import com.jinsung.safety_road_inclass.domain.risk.entity.CountermeasureStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CountermeasureResponse - 개선 대책 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "개선 대책 응답")
public class CountermeasureResponse {

    @Schema(description = "대책 ID", example = "1")
    private Long id;

    @Schema(description = "대책 내용", example = "안전 난간 설치")
    private String content;

    @Schema(description = "상태", example = "PLANNED")
    private CountermeasureStatus status;

    @Schema(description = "완료 예정일", example = "2025-12-20")
    private LocalDate dueDate;

    @Schema(description = "완료 시간", example = "2025-12-18T14:30:00")
    private LocalDateTime completedAt;

    @Schema(description = "작성자 이름", example = "김관리")
    private String createdByName;

    @Schema(description = "생성 시간", example = "2025-12-06T10:00:00")
    private LocalDateTime createdAt;

    public static CountermeasureResponse from(Countermeasure countermeasure) {
        return CountermeasureResponse.builder()
                .id(countermeasure.getId())
                .content(countermeasure.getContent())
                .status(countermeasure.getStatus())
                .dueDate(countermeasure.getDueDate())
                .completedAt(countermeasure.getCompletedAt())
                .createdByName(countermeasure.getCreatedBy().getName())
                .createdAt(countermeasure.getCreatedAt())
                .build();
    }
}


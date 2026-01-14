package com.jinsung.safety_road_inclass.domain.alert.dto;

import com.jinsung.safety_road_inclass.domain.alert.entity.Alert;
import com.jinsung.safety_road_inclass.domain.alert.entity.AlertType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * AlertResponse - 알림 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "알림 응답")
public class AlertResponse {

    @Schema(description = "알림 ID", example = "1")
    private Long id;

    @Schema(description = "알림 제목", example = "안전 교육 안내")
    private String title;

    @Schema(description = "알림 내용", example = "1월 15일 오후 2시 안전 교육이 진행됩니다.")
    private String message;

    @Schema(description = "알림 유형", example = "INFO")
    private AlertType type;

    @Schema(description = "우선순위", example = "0")
    private Integer priority;

    @Schema(description = "활성화 여부", example = "true")
    private Boolean active;

    @Schema(description = "표시 시작일", example = "2026-01-15T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "표시 종료일", example = "2026-01-31T23:59:59")
    private LocalDateTime endDate;

    @Schema(description = "작성자 이름", example = "관리자")
    private String createdByName;

    @Schema(description = "생성일", example = "2026-01-14T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2026-01-14T10:00:00")
    private LocalDateTime updatedAt;

    /**
     * Entity -> Response DTO 변환
     */
    public static AlertResponse from(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .type(alert.getType())
                .priority(alert.getPriority())
                .active(alert.getActive())
                .startDate(alert.getStartDate())
                .endDate(alert.getEndDate())
                .createdByName(alert.getCreatedBy() != null ? alert.getCreatedBy().getName() : null)
                .createdAt(alert.getCreatedAt())
                .updatedAt(alert.getUpdatedAt())
                .build();
    }
}

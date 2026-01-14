package com.jinsung.safety_road_inclass.domain.alert.dto;

import com.jinsung.safety_road_inclass.domain.alert.entity.AlertType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AlertRequest - 알림 생성/수정 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 생성/수정 요청")
public class AlertRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    @Schema(description = "알림 제목", example = "안전 교육 안내")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Size(max = 2000, message = "내용은 2000자 이하여야 합니다.")
    @Schema(description = "알림 내용", example = "1월 15일 오후 2시 안전 교육이 진행됩니다.")
    private String message;

    @Schema(description = "알림 유형", example = "INFO", defaultValue = "INFO")
    private AlertType type;

    @Schema(description = "우선순위 (높을수록 상단 표시)", example = "0", defaultValue = "0")
    private Integer priority;

    @Schema(description = "활성화 여부", example = "true", defaultValue = "true")
    private Boolean active;

    @Schema(description = "표시 시작일 (null이면 즉시)", example = "2026-01-15T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "표시 종료일 (null이면 무기한)", example = "2026-01-31T23:59:59")
    private LocalDateTime endDate;
}

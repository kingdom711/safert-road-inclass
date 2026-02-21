package com.jinsung.safety_road_inclass.domain.hazardcycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Hazard Cycle 조치 완료 요청")
public class CompletionUploadRequest {

    @Size(max = 2000, message = "note는 최대 2000자입니다.")
    @Schema(description = "조치 메모", example = "누전 차단기 점검 후 절연 조치 완료")
    private String note;
}

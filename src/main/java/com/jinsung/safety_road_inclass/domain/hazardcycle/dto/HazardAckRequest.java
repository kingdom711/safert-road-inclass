package com.jinsung.safety_road_inclass.domain.hazardcycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "동료 근로자 위험요인 확인 요청")
public class HazardAckRequest {

    @Size(max = 500)
    @Schema(description = "확인 코멘트 (선택)", example = "현장에서 확인했고, 즉시 조치 필요합니다.")
    private String comment;
}

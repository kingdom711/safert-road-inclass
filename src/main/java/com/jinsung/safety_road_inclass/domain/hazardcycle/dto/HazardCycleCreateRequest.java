package com.jinsung.safety_road_inclass.domain.hazardcycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Hazard Cycle 생성 요청")
public class HazardCycleCreateRequest {

    @Size(max = 2000, message = "description은 최대 2000자입니다.")
    @Schema(description = "위험 설명", example = "전기 패널 주변에 물기가 있습니다.")
    private String description;

    @Size(max = 500, message = "location은 최대 500자입니다.")
    @Schema(description = "위치 설명", example = "A동 3층 전기실 앞")
    private String location;

    @Size(max = 100, message = "clientTempId는 최대 100자입니다.")
    @Schema(description = "오프라인 중복 방지 임시 ID", example = "9fdf54f8-56e3-4d1c-9da2-3b87f2d1f6b2")
    private String clientTempId;
}

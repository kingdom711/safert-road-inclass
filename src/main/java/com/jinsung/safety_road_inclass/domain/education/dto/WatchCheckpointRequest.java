package com.jinsung.safety_road_inclass.domain.education.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * WatchCheckpointRequest - 영상 시청 체크포인트 요청 DTO
 *
 * 프론트엔드가 30초 간격으로 전송
 */
@Getter
@NoArgsConstructor
public class WatchCheckpointRequest {

    @NotBlank(message = "educationId는 필수입니다.")
    private String educationId;

    @NotNull
    @Min(value = 0, message = "checkpointSec는 0 이상이어야 합니다.")
    private Integer checkpointSec;

    @NotNull
    @Min(value = 1, message = "totalSec는 1 이상이어야 합니다.")
    private Integer totalSec;

    @NotNull
    private Boolean isPlaying;

    @NotNull
    private Boolean tabVisible;
}

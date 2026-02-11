package com.jinsung.safety_road_inclass.domain.gameprofile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UnlockSpecializationRequest - 전직 해금 요청 DTO
 */
@Getter
@NoArgsConstructor
public class UnlockSpecializationRequest {

    @NotBlank(message = "전직 ID는 필수입니다.")
    private String specId;

    private String educationProgress;
}

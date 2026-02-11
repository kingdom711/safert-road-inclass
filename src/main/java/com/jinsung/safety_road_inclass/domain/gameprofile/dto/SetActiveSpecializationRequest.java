package com.jinsung.safety_road_inclass.domain.gameprofile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SetActiveSpecializationRequest - 활성 전직 변경 요청 DTO
 */
@Getter
@NoArgsConstructor
public class SetActiveSpecializationRequest {

    @NotBlank(message = "전직 ID는 필수입니다.")
    private String specId;
}

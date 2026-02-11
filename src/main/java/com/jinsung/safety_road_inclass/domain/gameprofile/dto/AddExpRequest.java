package com.jinsung.safety_road_inclass.domain.gameprofile.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AddExpRequest - 경험치 추가 요청 DTO
 */
@Getter
@NoArgsConstructor
public class AddExpRequest {

    @NotNull(message = "경험치 양은 필수입니다.")
    @Min(value = 1, message = "경험치는 1 이상이어야 합니다.")
    private Integer amount;

    private String source;
}

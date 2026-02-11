package com.jinsung.safety_road_inclass.domain.gameprofile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SetRoleRequest - 게임 역할 설정 요청 DTO
 */
@Getter
@NoArgsConstructor
public class SetRoleRequest {

    @NotBlank(message = "역할은 필수입니다.")
    private String role;
}

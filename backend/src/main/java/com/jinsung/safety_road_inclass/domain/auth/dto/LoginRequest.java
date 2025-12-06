package com.jinsung.safety_road_inclass.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * LoginRequest - 로그인 요청 DTO
 */
@Schema(description = "로그인 요청")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginRequest {

    @Schema(description = "사용자 ID", example = "worker1")
    @NotBlank(message = "사용자 ID를 입력해주세요.")
    private String username;

    @Schema(description = "비밀번호", example = "password123")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}


package com.jinsung.safety_road_inclass.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SignupRequest - 회원가입 요청 DTO
 */
@Schema(description = "회원가입 요청")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupRequest {

    @Schema(description = "이메일 (로그인 ID로 사용)", example = "user@example.com")
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "비밀번호", example = "password123")
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
    private String password;

    @Schema(description = "닉네임", example = "김대리")
    @NotBlank(message = "닉네임을 입력해주세요.")
    private String name;

    public SignupRequest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }
}

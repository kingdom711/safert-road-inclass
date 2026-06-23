package com.jinsung.safety_road_inclass.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordResetConfirmRequest {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @Pattern(regexp = "\\d{6}", message = "인증 코드는 6자리 숫자입니다.")
    @NotBlank(message = "인증 코드를 입력해주세요.")
    private String code;

    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    private String newPassword;
}

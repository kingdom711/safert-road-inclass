package com.jinsung.safety_road_inclass.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PasswordResetRequest {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다.")
    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    private String newPassword;
}

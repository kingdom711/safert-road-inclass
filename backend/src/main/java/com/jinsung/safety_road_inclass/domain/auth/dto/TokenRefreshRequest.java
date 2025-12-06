package com.jinsung.safety_road_inclass.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * TokenRefreshRequest - 토큰 갱신 요청 DTO
 */
@Schema(description = "토큰 갱신 요청")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenRefreshRequest {

    @Schema(description = "Refresh Token")
    @NotBlank(message = "Refresh Token을 입력해주세요.")
    private String refreshToken;

    public TokenRefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}


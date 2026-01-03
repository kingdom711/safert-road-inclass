package com.jinsung.safety_road_inclass.domain.auth.dto;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * LoginResponse - 로그인 응답 DTO
 */
@Schema(description = "로그인 응답")
@Getter
@Builder
public class LoginResponse {

    @Schema(description = "Access Token (JWT)")
    private String accessToken;

    @Schema(description = "Refresh Token (JWT)")
    private String refreshToken;

    @Schema(description = "토큰 타입", example = "Bearer")
    private String tokenType;

    @Schema(description = "사용자 정보")
    private UserInfo user;

    public static LoginResponse of(String accessToken, String refreshToken, User user) {
        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .user(UserInfo.from(user))
            .build();
    }

    /**
     * UserInfo - 사용자 정보 DTO
     */
    @Schema(description = "사용자 정보")
    @Getter
    @Builder
    public static class UserInfo {
        
        @Schema(description = "사용자 ID", example = "1")
        private Long id;

        @Schema(description = "사용자명 (로그인 ID)", example = "worker1")
        private String username;

        @Schema(description = "이름", example = "김대리")
        private String name;

        @Schema(description = "역할", example = "ROLE_WORKER")
        private String role;

        @Schema(description = "이메일", example = "worker1@example.com")
        private String email;

        public static UserInfo from(User user) {
            return UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole().name())
                .email(user.getEmail())
                .build();
        }
    }
}


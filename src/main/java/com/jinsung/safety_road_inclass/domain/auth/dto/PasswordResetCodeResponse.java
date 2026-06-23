package com.jinsung.safety_road_inclass.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordResetCodeResponse {

    private boolean codeVisible;
    private String resetCode;
    private String message;
}

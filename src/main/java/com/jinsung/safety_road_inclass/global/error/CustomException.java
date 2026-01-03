package com.jinsung.safety_road_inclass.global.error;

import lombok.Getter;

/**
 * CustomException - 비즈니스 로직 예외
 * 
 * 사용 예시:
 * throw new CustomException(ErrorCode.USER_NOT_FOUND);
 * throw new CustomException(ErrorCode.INVALID_INPUT, "이메일 형식이 올바르지 않습니다");
 */
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String customMessage;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }

    public CustomException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }

    /**
     * 실제 응답에 사용될 메시지 반환
     * customMessage가 있으면 customMessage, 없으면 ErrorCode의 기본 메시지
     */
    public String getResponseMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }
}


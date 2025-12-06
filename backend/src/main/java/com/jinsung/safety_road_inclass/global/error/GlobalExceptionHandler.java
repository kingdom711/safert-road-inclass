package com.jinsung.safety_road_inclass.global.error;

import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler - 전역 예외 처리
 * 
 * 모든 예외를 ApiResponse 형태로 표준화하여 반환
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * CustomException 처리 (비즈니스 로직 예외)
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(
            CustomException e, HttpServletRequest request) {
        
        log.warn("CustomException: {} - {} [{}]", 
                e.getErrorCode().getCode(), 
                e.getResponseMessage(),
                request.getRequestURI());
        
        return ResponseEntity
            .status(e.getErrorCode().getHttpStatus())
            .body(ApiResponse.error(
                e.getErrorCode().getCode(), 
                e.getResponseMessage()
            ));
    }

    /**
     * Validation 예외 처리 (@Valid 실패)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        
        log.warn("Validation failed: {} [{}]", errors, request.getRequestURI());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(
                ErrorCode.INVALID_INPUT.getCode(),
                "입력 값 검증에 실패했습니다.",
                errors
            ));
    }

    /**
     * JSON 파싱 오류
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e, HttpServletRequest request) {
        
        log.warn("JSON parse error: {} [{}]", e.getMessage(), request.getRequestURI());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(
                ErrorCode.INVALID_INPUT.getCode(),
                "요청 본문을 파싱할 수 없습니다."
            ));
    }

    /**
     * 타입 불일치 예외
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        
        String message = String.format("'%s' 값이 올바르지 않습니다.", e.getName());
        log.warn("Type mismatch: {} [{}]", message, request.getRequestURI());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(
                ErrorCode.INVALID_TYPE.getCode(),
                message
            ));
    }

    /**
     * 필수 파라미터 누락
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(
            MissingServletRequestParameterException e, HttpServletRequest request) {
        
        String message = String.format("필수 파라미터 '%s'가 누락되었습니다.", e.getParameterName());
        log.warn("Missing parameter: {} [{}]", message, request.getRequestURI());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(
                ErrorCode.MISSING_PARAMETER.getCode(),
                message
            ));
    }

    /**
     * 404 Not Found (핸들러 없음)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(
            NoHandlerFoundException e, HttpServletRequest request) {
        
        log.warn("No handler found: {} {} [{}]", 
                e.getHttpMethod(), e.getRequestURL(), request.getRequestURI());
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(
                ErrorCode.RESOURCE_NOT_FOUND.getCode(),
                "요청한 API를 찾을 수 없습니다: " + e.getRequestURL()
            ));
    }

    /**
     * HTTP 메서드 불일치
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        
        log.warn("Method not allowed: {} [{}]", e.getMethod(), request.getRequestURI());
        
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(ApiResponse.error(
                ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                String.format("'%s' 메서드는 지원하지 않습니다.", e.getMethod())
            ));
    }

    /**
     * 파일 크기 초과
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(
            MaxUploadSizeExceededException e, HttpServletRequest request) {
        
        log.warn("File size exceeded [{}]", request.getRequestURI());
        
        return ResponseEntity
            .status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(ApiResponse.error(
                ErrorCode.FILE_SIZE_EXCEEDED.getCode(),
                "파일 크기가 10MB를 초과했습니다."
            ));
    }

    /**
     * Spring Security - 인증 실패
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException e, HttpServletRequest request) {
        
        log.warn("Authentication failed: {} [{}]", e.getMessage(), request.getRequestURI());
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error(
                ErrorCode.AUTH_UNAUTHORIZED.getCode(),
                "인증에 실패했습니다."
            ));
    }

    /**
     * Spring Security - 권한 부족
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException e, HttpServletRequest request) {
        
        log.warn("Access denied: {} [{}]", e.getMessage(), request.getRequestURI());
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(
                ErrorCode.AUTH_ACCESS_DENIED.getCode(),
                "접근 권한이 없습니다."
            ));
    }

    /**
     * 기타 모든 예외 (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(
            Exception e, HttpServletRequest request) {
        
        log.error("Unexpected error: {} [{}]", e.getMessage(), request.getRequestURI(), e);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            ));
    }
}


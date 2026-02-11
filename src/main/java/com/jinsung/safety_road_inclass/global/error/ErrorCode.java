package com.jinsung.safety_road_inclass.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * ErrorCode - 전역 에러 코드 정의
 * 
 * 네이밍 규칙: {도메인}_{에러유형}
 * 예: AUTH_INVALID_PASSWORD, CHECKLIST_NOT_FOUND
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ===== Common (공통) =====
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "입력 값이 올바르지 않습니다."),
    INVALID_TYPE(HttpStatus.BAD_REQUEST, "C002", "잘못된 타입입니다."),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "C003", "필수 파라미터가 누락되었습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C005", "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C006", "서버 내부 오류가 발생했습니다."),

    // ===== Auth (인증/인가) =====
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "인증이 필요합니다."),
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "유효하지 않은 토큰입니다."),
    AUTH_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "만료된 토큰입니다."),
    AUTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "A004", "접근 권한이 없습니다."),
    AUTH_INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "A005", "비밀번호가 일치하지 않습니다."),

    // ===== User (사용자) =====
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자입니다."),
    USER_INVALID_ROLE(HttpStatus.BAD_REQUEST, "U003", "유효하지 않은 역할입니다."),

    // ===== Template (템플릿) =====
    TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "템플릿을 찾을 수 없습니다."),
    TEMPLATE_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "T002", "템플릿 항목을 찾을 수 없습니다."),
    INVALID_TEMPLATE_ITEM(HttpStatus.BAD_REQUEST, "T003", "유효하지 않은 템플릿 항목입니다."),

    // ===== Checklist (체크리스트) =====
    CHECKLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "CL001", "체크리스트를 찾을 수 없습니다."),
    CHECKLIST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CL002", "체크리스트 항목을 찾을 수 없습니다."),
    INVALID_CHECKLIST_STATUS(HttpStatus.BAD_REQUEST, "CL003", "유효하지 않은 체크리스트 상태입니다."),
    CHECKLIST_ALREADY_SUBMITTED(HttpStatus.CONFLICT, "CL004", "이미 제출된 체크리스트입니다."),
    CHECKLIST_CANNOT_MODIFY(HttpStatus.CONFLICT, "CL005", "수정할 수 없는 체크리스트입니다."),

    // ===== Risk (위험성 평가) =====
    RISK_ASSESSMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "위험성 평가를 찾을 수 없습니다."),
    ALREADY_ASSESSED(HttpStatus.CONFLICT, "R002", "이미 평가된 항목입니다."),
    NOT_RISK_ITEM(HttpStatus.BAD_REQUEST, "R003", "위험 항목이 아닙니다."),
    INVALID_RISK_VALUE(HttpStatus.BAD_REQUEST, "R004", "빈도/강도는 1~5 사이의 값이어야 합니다."),

    // ===== Review (검토/승인) =====
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "RV001", "검토 내역을 찾을 수 없습니다."),
    INVALID_REVIEW_ACTION(HttpStatus.BAD_REQUEST, "RV002", "유효하지 않은 검토 액션입니다."),
    CANNOT_REVIEW_OWN_CHECKLIST(HttpStatus.FORBIDDEN, "RV003", "본인의 체크리스트는 검토할 수 없습니다."),

    // ===== File (파일) =====
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "F001", "파일 업로드에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "F002", "파일을 찾을 수 없습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "F003", "허용되지 않은 파일 형식입니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "F004", "파일 크기가 제한을 초과했습니다."),

    // ===== AI (AI 분석) =====
    AI_ANALYSIS_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "AI001", "AI 분석에 실패했습니다."),
    AI_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI002", "AI 서비스에 연결할 수 없습니다."),

    // ===== Alert (알림) =====
    ALERT_NOT_FOUND(HttpStatus.NOT_FOUND, "AL001", "알림을 찾을 수 없습니다."),

    // ===== Points (포인트) =====
    POINTS_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "포인트 정보를 찾을 수 없습니다."),
    POINTS_INSUFFICIENT(HttpStatus.BAD_REQUEST, "P002", "포인트가 부족합니다."),
    POINTS_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "P003", "유효하지 않은 포인트 금액입니다."),

    // ===== Attendance (출석) =====
    ATTENDANCE_ALREADY_CHECKED_IN(HttpStatus.CONFLICT, "AT001", "오늘 이미 출석하였습니다."),
    ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "AT002", "출석 기록을 찾을 수 없습니다."),

    // ===== GameProfile (게임 프로필) =====
    GAMEPROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "GP001", "게임 프로필을 찾을 수 없습니다."),
    GAMEPROFILE_SPECIALIZATION_NOT_FOUND(HttpStatus.NOT_FOUND, "GP002", "해금되지 않은 전직입니다."),
    GAMEPROFILE_SPECIALIZATION_ALREADY_UNLOCKED(HttpStatus.CONFLICT, "GP003", "이미 해금된 전직입니다."),
    GAMEPROFILE_INVALID_EXP(HttpStatus.BAD_REQUEST, "GP004", "유효하지 않은 경험치 값입니다."),

    // ===== Gold (골드) =====
    GOLD_NOT_FOUND(HttpStatus.NOT_FOUND, "G001", "골드 정보를 찾을 수 없습니다."),
    GOLD_INSUFFICIENT(HttpStatus.BAD_REQUEST, "G002", "골드가 부족합니다."),
    GOLD_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "G003", "유효하지 않은 골드 금액입니다."),

    // ===== Exchange (교환) =====
    EXCHANGE_MINIMUM_NOT_MET(HttpStatus.BAD_REQUEST, "EX001", "최소 교환 금액에 미달합니다."),
    EXCHANGE_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "EX002", "교환 가능한 단위로 입력해주세요."),

    // ===== Reward (보상) =====
    REWARD_NOT_FOUND(HttpStatus.NOT_FOUND, "RW001", "보상을 찾을 수 없습니다."),
    REWARD_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "RW002", "현재 교환 불가능한 보상입니다."),
    REWARD_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "RW003", "보상이 품절되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

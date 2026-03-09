package com.jinsung.safety_road_inclass.domain.education.controller;

import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import com.jinsung.safety_road_inclass.domain.education.dto.*;
import com.jinsung.safety_road_inclass.domain.education.service.EducationService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * EducationController - 교육 수료 기록 API
 *
 * 엔드포인트:
 *   POST /api/v1/education/checkpoint  - 영상 시청 체크포인트 (30초 간격)
 *   POST /api/v1/education/quiz-submit - 퀴즈 개별 답안 저장
 *   POST /api/v1/education/complete    - 교육 완료 최종 기록
 */
@Tag(name = "Education", description = "교육 수료 증거 기록 API")
@RestController
@RequestMapping("/api/v1/education")
@RequiredArgsConstructor
public class EducationController {

    private final EducationService educationService;

    // ─── 1. 영상 시청 체크포인트 ─────────────────────────────────────

    @Operation(
            summary = "영상 시청 체크포인트 저장",
            description = "프론트엔드가 영상 재생 중 30초 간격으로 현재 위치를 서버에 전송합니다. " +
                    "서버 타임스탬프로 기록되어 실제 시청을 증명하는 핵심 증거가 됩니다."
    )
    @PostMapping("/checkpoint")
    public ApiResponse<WatchCheckpointResponse> saveCheckpoint(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody WatchCheckpointRequest request) {

        return ApiResponse.success(
                educationService.saveCheckpoint(principal.userId(), request)
        );
    }

    // ─── 2. 퀴즈 답안 저장 ───────────────────────────────────────────

    @Operation(
            summary = "퀴즈 개별 답안 저장",
            description = "퀴즈 제출 시 문항별 선택지와 소요 시간을 서버에 저장합니다. " +
                    "재시도 이력 전체가 보존되어 '직접 풀었음'을 증명합니다."
    )
    @PostMapping("/quiz-submit")
    public ApiResponse<QuizSubmitResponse> submitQuiz(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody QuizSubmitRequest request) {

        return ApiResponse.success(
                educationService.saveQuizAnswers(principal.userId(), request)
        );
    }

    // ─── 3. 교육 완료 최종 기록 ──────────────────────────────────────

    @Operation(
            summary = "교육 완료 최종 기록",
            description = "영상 시청과 퀴즈를 모두 마친 후 최종 수료 기록을 저장합니다. " +
                    "SHA-256 해시 체인을 생성하여 레코드 위변조를 탐지할 수 있습니다. " +
                    "수료증 번호(certNumber)가 발급됩니다."
    )
    @PostMapping("/complete")
    public ApiResponse<EducationCompleteResponse> complete(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody EducationCompleteRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        return ApiResponse.success(
                educationService.complete(principal.userId(), request, ipAddress, userAgent)
        );
    }

    // ─── 내부 유틸 ───────────────────────────────────────────────────

    /**
     * 실제 클라이언트 IP 추출 (프록시/로드밸런서 헤더 우선)
     */
    private String extractClientIp(HttpServletRequest request) {
        String[] ipHeaders = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_CLIENT_IP"
        };

        for (String header : ipHeaders) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For는 "client, proxy1, proxy2" 형태일 수 있음
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}

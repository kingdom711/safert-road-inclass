package com.jinsung.safety_road_inclass.domain.education.dto;

import com.jinsung.safety_road_inclass.domain.education.entity.EducationType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * EducationCompleteRequest - 교육 완료 최종 기록 요청 DTO
 *
 * 교육(영상 + 퀴즈)을 모두 마친 뒤 호출
 * 서버는 completedAt을 서버 시각으로 기록하고, SHA-256 해시 체인을 생성
 */
@Getter
@NoArgsConstructor
public class EducationCompleteRequest {

    @NotBlank(message = "educationId는 필수입니다.")
    private String educationId;

    @NotBlank(message = "educationTitle은 필수입니다.")
    private String educationTitle;

    @NotNull(message = "educationType은 필수입니다.")
    private EducationType educationType;

    @NotBlank(message = "startedAt은 필수입니다.")
    private String startedAt;         // ISO-8601 형식 (클라이언트 교육 시작 시각)

    @NotNull
    @Min(0)
    private Integer videoWatchSeconds;

    @NotNull
    @Min(1)
    private Integer videoTotalSeconds;

    @NotNull
    @DecimalMin("0.0000")
    @DecimalMax("1.0000")
    private BigDecimal videoWatchRatio;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer quizScore;

    @NotNull
    private Boolean quizPassed;

    @NotNull
    @Min(1)
    private Integer attemptCount;

    private String deviceHash;       // 디바이스 핑거프린트 (선택)
}

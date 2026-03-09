package com.jinsung.safety_road_inclass.domain.education.dto;

import com.jinsung.safety_road_inclass.domain.education.entity.EducationCompletion;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * EducationCompleteResponse - 교육 완료 기록 결과 DTO
 */
@Getter
@Builder
public class EducationCompleteResponse {

    private Long   completionId;       // 수료 기록 ID
    private String certNumber;         // 수료증 번호
    private String educationId;
    private String educationTitle;
    private String completedAt;        // 서버 기록 완료 시각
    private int    quizScore;
    private boolean quizPassed;
    private BigDecimal videoWatchRatio;
    private String integrityHash;      // SHA-256 무결성 해시

    public static EducationCompleteResponse from(EducationCompletion entity) {
        return EducationCompleteResponse.builder()
                .completionId(entity.getId())
                .certNumber(entity.getCertNumber())
                .educationId(entity.getEducationId())
                .educationTitle(entity.getEducationTitle())
                .completedAt(entity.getCompletedAt().toString())
                .quizScore(entity.getQuizScore())
                .quizPassed(entity.isQuizPassed())
                .videoWatchRatio(entity.getVideoWatchRatio())
                .integrityHash(entity.getIntegrityHash())
                .build();
    }
}

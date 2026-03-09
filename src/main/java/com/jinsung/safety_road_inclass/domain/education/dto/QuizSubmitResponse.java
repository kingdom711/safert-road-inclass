package com.jinsung.safety_road_inclass.domain.education.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * QuizSubmitResponse - 퀴즈 제출 결과 DTO
 */
@Getter
@Builder
public class QuizSubmitResponse {

    private int savedCount;      // 저장된 답안 수
    private int attemptNumber;   // 시도 번호
    private String savedAt;      // 서버 저장 시각
}

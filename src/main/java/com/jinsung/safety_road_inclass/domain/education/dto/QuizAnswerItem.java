package com.jinsung.safety_road_inclass.domain.education.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * QuizAnswerItem - 문항별 답안 단일 항목
 */
@Getter
@NoArgsConstructor
public class QuizAnswerItem {

    @NotBlank(message = "questionId는 필수입니다.")
    private String questionId;

    @NotNull
    @Min(value = 0)
    private Integer selectedOption;  // 선택한 보기 번호 (0-based)

    private Integer timeSpentSec;    // 해당 문항 소요 시간(초), nullable
}

package com.jinsung.safety_road_inclass.domain.education.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * QuizSubmitRequest - 퀴즈 제출 요청 DTO
 *
 * 프론트엔드가 퀴즈 완료 시 전체 답안 목록을 전송
 */
@Getter
@NoArgsConstructor
public class QuizSubmitRequest {

    @NotBlank(message = "educationId는 필수입니다.")
    private String educationId;

    @NotNull
    @Min(value = 1)
    private Integer attemptNumber;   // 몇 번째 시도인지

    @Valid
    @NotEmpty(message = "답안 목록은 비어있을 수 없습니다.")
    private List<QuizAnswerItem> answers;
}

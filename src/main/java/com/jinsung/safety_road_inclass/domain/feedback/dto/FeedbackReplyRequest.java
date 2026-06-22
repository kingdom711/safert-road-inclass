package com.jinsung.safety_road_inclass.domain.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FeedbackReplyRequest {

    @NotBlank(message = "답변 내용은 필수입니다.")
    @Size(max = 4000, message = "답변은 4000자 이하여야 합니다.")
    private String reply;
}

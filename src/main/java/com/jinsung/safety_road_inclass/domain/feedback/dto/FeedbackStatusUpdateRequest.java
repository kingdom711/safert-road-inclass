package com.jinsung.safety_road_inclass.domain.feedback.dto;

import com.jinsung.safety_road_inclass.domain.feedback.entity.FeedbackStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FeedbackStatusUpdateRequest {

    @NotNull(message = "상태는 필수입니다.")
    private FeedbackStatus status;
}

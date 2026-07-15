package com.jinsung.safety_road_inclass.domain.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NoticeCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 120, message = "제목은 120자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Size(max = 4000, message = "내용은 4000자 이하여야 합니다.")
    private String content;
}

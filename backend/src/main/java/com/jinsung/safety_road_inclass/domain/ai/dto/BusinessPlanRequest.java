package com.jinsung.safety_road_inclass.domain.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * GEMS AI 분석 요청 DTO
 * - 프론트엔드 가이드 형식에 맞춤
 */
@Getter
@Setter
@NoArgsConstructor
public class BusinessPlanRequest {
    
    /**
     * 입력 유형: TEXT, PHOTO, BOTH
     */
    @NotBlank(message = "inputType은 필수입니다.")
    private String inputType;
    
    /**
     * 분석할 텍스트 내용
     */
    @NotBlank(message = "inputText는 필수입니다.")
    private String inputText;
    
    /**
     * 사진 ID (이미지 분석 시)
     */
    private String photoId;
    
    /**
     * 컨텍스트 정보
     */
    @Valid
    private Context context;
    
    @Getter
    @Setter
    @NoArgsConstructor
    public static class Context {
        private String workType;      // 작업 유형 (construction, electrical, etc.)
        private String location;      // 작업 위치
        private Integer workerCount;  // 작업자 수
        private String currentTask;   // 현재 수행 중인 작업
    }
}


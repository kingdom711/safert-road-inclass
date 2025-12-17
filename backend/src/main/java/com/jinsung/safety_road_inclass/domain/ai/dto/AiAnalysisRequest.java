package com.jinsung.safety_road_inclass.domain.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * AI 분석 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class AiAnalysisRequest {
    
    private Long checklistId;
    private String content;
    
    public AiAnalysisRequest(Long checklistId, String content) {
        this.checklistId = checklistId;
        this.content = content;
    }
}



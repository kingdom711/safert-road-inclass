package com.jinsung.safety_road_inclass.domain.gameprofile.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * AddExpResponse - 경험치 추가 결과 응답 DTO
 */
@Getter
@Builder
public class AddExpResponse {
    private int addedExp;
    private int currentLevel;
    private int currentExp;
    private int expToNext;
    private int levelsGained;
    private String source;
}

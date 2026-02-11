package com.jinsung.safety_road_inclass.domain.gameprofile.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * GameProfileUpdateRequest - 게임 프로필 업데이트 요청 DTO
 */
@Getter
@NoArgsConstructor
public class GameProfileUpdateRequest {
    private String gameRole;
    private String activeSpecialization;
}

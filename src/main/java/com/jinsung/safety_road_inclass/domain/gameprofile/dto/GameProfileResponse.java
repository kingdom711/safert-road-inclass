package com.jinsung.safety_road_inclass.domain.gameprofile.dto;

import com.jinsung.safety_road_inclass.domain.gameprofile.entity.UserGameProfile;
import lombok.Builder;
import lombok.Getter;

/**
 * GameProfileResponse - 게임 프로필 응답 DTO
 */
@Getter
@Builder
public class GameProfileResponse {
    private int level;
    private int exp;
    private int expToNext;
    private String gameRole;
    private String activeSpecialization;
    private int totalQuestsCompleted;

    public static GameProfileResponse from(UserGameProfile profile) {
        return GameProfileResponse.builder()
                .level(profile.getLevel())
                .exp(profile.getExp())
                .expToNext(profile.getExpToNext())
                .gameRole(profile.getGameRole())
                .activeSpecialization(profile.getActiveSpecialization())
                .totalQuestsCompleted(profile.getTotalQuestsCompleted())
                .build();
    }

    public static GameProfileResponse empty() {
        return GameProfileResponse.builder()
                .level(1)
                .exp(0)
                .expToNext(100)
                .totalQuestsCompleted(0)
                .build();
    }
}

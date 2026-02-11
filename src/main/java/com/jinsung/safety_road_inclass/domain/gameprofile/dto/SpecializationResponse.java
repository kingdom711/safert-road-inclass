package com.jinsung.safety_road_inclass.domain.gameprofile.dto;

import com.jinsung.safety_road_inclass.domain.gameprofile.entity.UserSpecialization;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * SpecializationResponse - 전직 정보 응답 DTO
 */
@Getter
@Builder
public class SpecializationResponse {
    private String specId;
    private LocalDateTime unlockedAt;
    private String educationProgress;

    public static SpecializationResponse from(UserSpecialization spec) {
        return SpecializationResponse.builder()
                .specId(spec.getSpecId())
                .unlockedAt(spec.getUnlockedAt())
                .educationProgress(spec.getEducationProgress())
                .build();
    }
}

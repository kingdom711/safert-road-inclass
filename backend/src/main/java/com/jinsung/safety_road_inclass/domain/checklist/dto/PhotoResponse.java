package com.jinsung.safety_road_inclass.domain.checklist.dto;

import com.jinsung.safety_road_inclass.domain.checklist.entity.Photo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * PhotoResponse - 사진 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "사진 응답")
public class PhotoResponse {

    @Schema(description = "사진 ID", example = "1")
    private Long id;

    @Schema(description = "원본 파일명", example = "site_photo.jpg")
    private String originalName;

    @Schema(description = "파일 URL", example = "/api/v1/files/550e8400-e29b-41d4-a716-446655440000.jpg")
    private String url;

    @Schema(description = "파일 크기 (bytes)", example = "2048576")
    private Long fileSize;

    @Schema(description = "사진 유형", example = "EVIDENCE")
    private String photoType;

    public static PhotoResponse from(Photo photo) {
        return PhotoResponse.builder()
                .id(photo.getId())
                .originalName(photo.getOriginalName())
                .url(photo.getUrl())
                .fileSize(photo.getFileSize())
                .photoType(photo.getPhotoType() != null ? photo.getPhotoType().name() : null)
                .build();
    }
}


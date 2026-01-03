package com.jinsung.safety_road_inclass.domain.checklist.dto;

import com.jinsung.safety_road_inclass.domain.checklist.entity.Checklist;
import com.jinsung.safety_road_inclass.domain.checklist.entity.ChecklistStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ChecklistResponse - 체크리스트 상세 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "체크리스트 상세 응답")
public class ChecklistResponse {

    @Schema(description = "체크리스트 ID", example = "1")
    private Long id;

    @Schema(description = "템플릿 ID", example = "1")
    private Long templateId;

    @Schema(description = "템플릿 이름", example = "고소작업 안전점검표")
    private String templateTitle;

    @Schema(description = "작성자 ID", example = "1")
    private Long createdById;

    @Schema(description = "작성자 이름", example = "김작업")
    private String createdByName;

    @Schema(description = "현장명", example = "서울 강남구 테헤란로 현장")
    private String siteName;

    @Schema(description = "작업일", example = "2025-12-06")
    private LocalDate workDate;

    @Schema(description = "상태", example = "SUBMITTED")
    private ChecklistStatus status;

    @Schema(description = "비고", example = "특이사항 없음")
    private String remarks;

    @Schema(description = "제출 시간", example = "2025-12-06T10:30:00")
    private LocalDateTime submittedAt;

    @Schema(description = "위험 항목 개수", example = "2")
    private Long riskCount;

    @Schema(description = "체크리스트 항목 목록")
    private List<ChecklistItemResponse> items;

    @Schema(description = "사진 목록")
    private List<PhotoResponse> photos;

    @Schema(description = "생성 시간", example = "2025-12-06T09:00:00")
    private LocalDateTime createdAt;

    public static ChecklistResponse from(Checklist checklist) {
        return ChecklistResponse.builder()
                .id(checklist.getId())
                .templateId(checklist.getTemplate().getId())
                .templateTitle(checklist.getTemplate().getTitle())
                .createdById(checklist.getCreatedBy().getId())
                .createdByName(checklist.getCreatedBy().getName())
                .siteName(checklist.getSiteName())
                .workDate(checklist.getWorkDate())
                .status(checklist.getStatus())
                .remarks(checklist.getRemarks())
                .submittedAt(checklist.getSubmittedAt())
                .riskCount(checklist.getRiskCount())
                .items(checklist.getItems().stream()
                        .map(ChecklistItemResponse::from)
                        .toList())
                .photos(checklist.getPhotos().stream()
                        .map(PhotoResponse::from)
                        .toList())
                .createdAt(checklist.getCreatedAt())
                .build();
    }
}


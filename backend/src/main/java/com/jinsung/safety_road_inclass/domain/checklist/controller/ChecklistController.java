package com.jinsung.safety_road_inclass.domain.checklist.controller;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.checklist.dto.ChecklistListResponse;
import com.jinsung.safety_road_inclass.domain.checklist.dto.ChecklistRequest;
import com.jinsung.safety_road_inclass.domain.checklist.dto.ChecklistResponse;
import com.jinsung.safety_road_inclass.domain.checklist.entity.ChecklistStatus;
import com.jinsung.safety_road_inclass.domain.checklist.service.ChecklistService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * ChecklistController - 체크리스트 REST API
 */
@Tag(name = "Checklist", description = "체크리스트 관리 API")
@RestController
@RequestMapping("/api/v1/checklists")
@RequiredArgsConstructor
@Slf4j
public class ChecklistController {

    private final ChecklistService checklistService;

    @Operation(summary = "체크리스트 제출", description = "작업자가 현장에서 작성한 체크리스트를 제출합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ChecklistResponse> submitChecklist(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
            @Valid @RequestPart("request") ChecklistRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        
        log.info("체크리스트 제출 요청: userId={}, templateId={}, siteName={}", 
                 currentUser.getId(), request.getTemplateId(), request.getSiteName());
        
        ChecklistResponse response = checklistService.submit(request, files, currentUser);
        
        return ApiResponse.success(response);
    }

    @Operation(summary = "내 체크리스트 목록 조회", description = "로그인한 사용자가 작성한 체크리스트 목록을 조회합니다 (페이징).")
    @GetMapping("/my")
    public ApiResponse<Page<ChecklistListResponse>> getMyChecklists(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Page<ChecklistListResponse> checklists = checklistService.getMyChecklists(currentUser, pageable);
        
        return ApiResponse.success(checklists);
    }

    @Operation(summary = "체크리스트 상세 조회", description = "체크리스트의 상세 정보를 조회합니다.")
    @GetMapping("/{checklistId}")
    public ApiResponse<ChecklistResponse> getChecklistDetail(
            @Parameter(hidden = true) @AuthenticationPrincipal User currentUser,
            @PathVariable Long checklistId) {
        
        ChecklistResponse response = checklistService.getChecklistDetail(checklistId, currentUser);
        
        return ApiResponse.success(response);
    }

    @Operation(summary = "상태별 체크리스트 조회", description = "특정 상태의 체크리스트 목록을 조회합니다. (SUPERVISOR/ADMIN 권한 필요)")
    @GetMapping("/status/{status}")
    public ApiResponse<List<ChecklistListResponse>> getChecklistsByStatus(
            @PathVariable ChecklistStatus status) {
        
        List<ChecklistListResponse> checklists = checklistService.getChecklistsByStatus(status);
        
        return ApiResponse.success(checklists);
    }

    @Operation(summary = "위험 항목이 있는 체크리스트 조회", description = "위험 플래그가 있는 체크리스트 목록을 조회합니다.")
    @GetMapping("/with-risk")
    public ApiResponse<List<ChecklistListResponse>> getChecklistsWithRisk(
            @RequestParam(defaultValue = "SUBMITTED") ChecklistStatus status) {
        
        List<ChecklistListResponse> checklists = checklistService.getChecklistsWithRisk(status);
        
        return ApiResponse.success(checklists);
    }
}


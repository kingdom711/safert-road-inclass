package com.jinsung.safety_road_inclass.domain.hazardcycle.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import com.jinsung.safety_road_inclass.domain.hazardcycle.dto.*;
import com.jinsung.safety_road_inclass.domain.hazardcycle.service.HazardCycleService;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * HazardCycleController - Hazard Cycle API
 */
@Tag(name = "Hazard Cycle", description = "위험 발견/조치 완료 1사이클 API")
@RestController
@RequestMapping("/api/v1/hazard-cycles")
@RequiredArgsConstructor
@Slf4j
public class HazardCycleController {

    private final HazardCycleService hazardCycleService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "Hazard Cycle 생성", description = "위험 사진 업로드 후 AI 분석 및 1단계 보상을 지급합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<HazardCycleResponse> createCycle(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestPart("photo") MultipartFile photo,
            @Valid @RequestPart(value = "request", required = false) HazardCycleCreateRequest request) {

        HazardCycleResponse response = hazardCycleService.createHazardCycle(
                principal.userId(),
                photo,
                request);

        return ApiResponse.success(response);
    }

    @Operation(summary = "Hazard Cycle 조치 완료", description = "조치 완료 사진 업로드 후 2단계 보상을 지급합니다.")
    @PostMapping(value = "/{cycleId}/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<HazardCycleResponse> completeCycle(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long cycleId,
            @RequestPart("photo") MultipartFile photo,
            @Valid @RequestPart(value = "request", required = false) CompletionUploadRequest request) {

        HazardCycleResponse response = hazardCycleService.completeCycle(
                principal.userId(),
                cycleId,
                photo,
                request);

        return ApiResponse.success(response);
    }

    @Operation(summary = "내 Hazard Cycle 목록", description = "내가 생성한 사이클 목록을 조회합니다.")
    @GetMapping("/my")
    public ApiResponse<Page<HazardCycleResponse>> getMyCycles(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam(defaultValue = "ALL") String status,
            @PageableDefault(size = 20, sort = "reportedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<HazardCycleResponse> response = hazardCycleService.getMyCycles(principal.userId(), status, pageable);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Hazard Cycle 상세 조회", description = "사이클 상세 정보를 조회합니다.")
    @GetMapping("/{cycleId}")
    public ApiResponse<HazardCycleResponse> getCycleDetail(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long cycleId) {

        HazardCycleResponse response = hazardCycleService.getCycleDetail(principal.userId(), cycleId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "오프라인 사이클 동기화", description = "오프라인에서 저장된 사이클을 배치 동기화합니다.")
    @PostMapping(value = "/sync", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CycleSyncResponse> syncOfflineCycles(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestPart("cycles") String cyclesJson,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos) {

        List<CycleSyncRequest> cycles = parseCycles(cyclesJson);

        CycleSyncResponse response = hazardCycleService.syncOfflineCycles(
                principal.userId(),
                cycles,
                photos != null ? photos : Collections.emptyList());

        return ApiResponse.success(response);
    }

    @Operation(summary = "Hazard Cycle 통계", description = "내 Hazard Cycle 완료율/위험등급 분포 통계를 조회합니다.")
    @GetMapping("/stats")
    public ApiResponse<CycleStatsResponse> getStats(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {

        CycleStatsResponse response = hazardCycleService.getStats(principal.userId());
        return ApiResponse.success(response);
    }

    private List<CycleSyncRequest> parseCycles(String cyclesJson) {
        try {
            List<CycleSyncRequest> cycles = objectMapper.readValue(cyclesJson, new TypeReference<List<CycleSyncRequest>>() {
            });
            if (cycles == null || cycles.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "동기화할 cycles 데이터가 비어있습니다.");
            }
            return cycles;
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "cycles JSON 파싱에 실패했습니다.");
        }
    }
}

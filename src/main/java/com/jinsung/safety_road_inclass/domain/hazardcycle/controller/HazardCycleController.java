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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

    @Operation(summary = "Hazard Cycle 요약 조회(공유용)",
            description = "동료 확인(peer ack)을 위해 소유권 검증 없이 사이클 요약을 반환합니다. 인증된 사용자라면 누구나 접근 가능.")
    @GetMapping("/{cycleId}/summary")
    public ApiResponse<HazardCycleResponse> getCycleSummary(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long cycleId) {

        HazardCycleResponse response = hazardCycleService.getCycleSummary(cycleId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "동료 근로자 위험요인 확인",
            description = "산안법 제36조 2항 근로자 참여 증빙을 위한 peer ack. 본인 사이클 ack 불가, 중복 ack 불가.")
    @PostMapping(value = "/{cycleId}/ack", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<HazardAckResponse> ackCycle(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long cycleId,
            @Valid @RequestBody(required = false) HazardAckRequest request) {

        HazardAckResponse response = hazardCycleService.ackHazardCycle(principal.userId(), cycleId, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "Hazard Cycle 확인자 목록", description = "이 사이클을 확인한 동료 근로자 목록을 조회합니다.")
    @GetMapping("/{cycleId}/acks")
    public ApiResponse<List<HazardAckResponse>> listAcks(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long cycleId) {

        List<HazardAckResponse> response = hazardCycleService.listAcks(cycleId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "위험성평가표 PDF 다운로드",
            description = "고용노동부 고시 제2023-19호 준수 양식으로 위험성평가표 PDF를 생성합니다. AI 분석이 완료된 사이클만 발급 가능합니다.")
    @GetMapping(value = "/{cycleId}/risk-assessment.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadRiskAssessmentPdf(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long cycleId) {

        byte[] pdf = hazardCycleService.renderRiskAssessmentPdf(principal.userId(), cycleId);

        String filename = "risk-assessment-" + cycleId + ".pdf";
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded)
                .body(pdf);
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

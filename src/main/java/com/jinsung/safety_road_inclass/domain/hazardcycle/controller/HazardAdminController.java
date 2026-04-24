package com.jinsung.safety_road_inclass.domain.hazardcycle.controller;

import com.jinsung.safety_road_inclass.domain.hazardcycle.service.HazardCycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Hazard Cycle 관리자 API — 고용노동부 제출용 월간 대장 등.
 * SecurityConfig가 /api/v1/admin/** 을 ADMIN/SAFETY_MANAGER 권한으로 제한한다.
 */
@Tag(name = "Hazard Cycle Admin", description = "Hazard Cycle 관리자 API")
@RestController
@RequestMapping("/api/v1/admin/hazard-cycles")
@RequiredArgsConstructor
@Slf4j
public class HazardAdminController {

    private static final String EXCEL_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final HazardCycleService hazardCycleService;

    @Operation(summary = "월간 위험요인 개선대장 Excel 다운로드",
            description = "해당 연/월의 모든 Hazard Cycle을 고용노동부 점검관 제출 양식으로 내보냅니다. ADMIN/SAFETY_MANAGER 전용.")
    @GetMapping(value = "/monthly-log.xlsx", produces = EXCEL_MIME)
    public ResponseEntity<byte[]> downloadMonthlyLog(
            @RequestParam int year,
            @RequestParam int month) {

        byte[] xlsx = hazardCycleService.renderMonthlyLogExcel(year, month);

        String filename = String.format("hazard-monthly-log-%d-%02d.xlsx", year, month);
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, EXCEL_MIME)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encoded)
                .body(xlsx);
    }
}

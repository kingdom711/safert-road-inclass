package com.jinsung.safety_road_inclass.global.config;

import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * HealthCheckController - 서버 상태 확인
 */
@Tag(name = "Health", description = "서버 상태 확인 API")
@RestController
@RequestMapping("/api/v1")
public class HealthCheckController {

    @Operation(summary = "서버 상태 확인", description = "서버가 정상 동작 중인지 확인합니다.")
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> healthCheck() {
        return ApiResponse.success(Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "service", "Safety Road API"
        ));
    }
}


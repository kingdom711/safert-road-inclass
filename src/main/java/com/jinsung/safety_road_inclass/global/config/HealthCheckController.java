package com.jinsung.safety_road_inclass.global.config;

import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import com.jinsung.safety_road_inclass.global.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * HealthCheckController - 서버 상태 확인
 */
@Tag(name = "Health", description = "서버 상태 확인 API")
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthCheckController {

    private final Environment environment;

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Value("${info.app.version:0.0.1-SNAPSHOT}")
    private String applicationVersion;

    @Operation(
        summary = "상세 헬스체크", 
        description = "서버의 상세 상태 정보를 반환합니다. 상태, 타임스탬프, 애플리케이션 이름, 버전, 환경 정보를 포함합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<HealthResponse>> healthCheck() {
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        String activeProfile = activeProfiles.isEmpty() ? "default" : String.join(",", activeProfiles);

        HealthResponse response = HealthResponse.builder()
                .status("UP")
                .timestamp(LocalDateTime.now())
                .application(applicationName)
                .version(applicationVersion)
                .environment(activeProfile)
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "간단한 헬스체크", 
        description = "로드밸런서나 모니터링 시스템에서 사용하는 간단한 헬스체크 엔드포인트입니다."
    )
    @GetMapping("/ping")
    public ResponseEntity<ApiResponse<HealthResponse.PingResponse>> ping() {
        HealthResponse.PingResponse response = HealthResponse.PingResponse.builder()
                .status("UP")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * CloudType 헬스체크용 루트 경로 엔드포인트
     * CloudType이 루트 경로(/)를 헬스체크로 사용합니다.
     */
    @GetMapping("/")
    public ResponseEntity<ApiResponse<HealthResponse.PingResponse>> root() {
        HealthResponse.PingResponse response = HealthResponse.PingResponse.builder()
                .status("UP")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}


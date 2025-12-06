package com.jinsung.safety_road_inclass.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 3.0 Configuration
 * - API 문서 자동 생성
 * - JWT 인증 헤더 설정
 * 
 * Swagger UI: http://localhost:8080/swagger-ui/index.html
 * API Docs: http://localhost:8080/api-docs
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(apiInfo())
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Local Development")
            ))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme())
            );
    }

    private Info apiInfo() {
        return new Info()
            .title("Safety Road API")
            .description("""
                ## 안전의 길 (Safety Road) - 스마트 안전관리 플랫폼 API
                
                ### 주요 기능
                - **인증/인가**: JWT 기반 로그인 및 역할별 접근 제어
                - **체크리스트**: 현장 안전 점검표 작성 및 제출
                - **위험성 평가**: 빈도×강도 기반 위험도 자동 계산
                - **검토/승인**: 관리자 결재 워크플로우
                - **AI 분석**: 작업 사진 자동 위험 분석
                
                ### 인증 방법
                1. `/api/v1/auth/login` API로 로그인하여 Access Token 발급
                2. 우측 상단 **Authorize** 버튼 클릭
                3. `Bearer {token}` 형식으로 토큰 입력
                """)
            .version("1.0.0")
            .contact(new Contact()
                .name("Safety Road Team")
                .email("support@safetyroad.com")
            )
            .license(new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT")
            );
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
            .name(SECURITY_SCHEME_NAME)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .description("JWT Access Token을 입력하세요. (Bearer 접두어 불필요)");
    }
}


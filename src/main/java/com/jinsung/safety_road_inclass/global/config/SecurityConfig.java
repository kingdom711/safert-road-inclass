package com.jinsung.safety_road_inclass.global.config;

import com.jinsung.safety_road_inclass.domain.auth.service.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security Configuration
 * - JWT 기반 인증 (Stateless)
 * - 역할 기반 접근 제어 (RBAC)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (JWT 사용)
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 관리: Stateless (JWT 사용)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // H2 Console을 위한 Frame 허용 (개발 환경)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // URL 기반 접근 제어
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (인증 불필요)
                        .requestMatchers("/").permitAll() // CloudType 헬스체크용 루트 경로
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/health/**").permitAll() // health와 health/ping 모두 허용
                        .requestMatchers("/api/v1/ai/**").permitAll() // AI 분석 API (레거시)
                        .requestMatchers("/api/v1/business-plan/**").permitAll() // GEMS AI API (프론트엔드 연동)
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // Role-based access control
                        .requestMatchers("/api/v1/reviews/**").hasAnyRole("SUPERVISOR", "SAFETY_MANAGER")
                        .requestMatchers("/api/v1/admin/**").hasRole("SAFETY_MANAGER")

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated())

                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 설정
     * 프론트엔드 개발 서버에서의 요청을 허용합니다.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin (프론트엔드 개발 서버)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000", // Vite 기본 포트
                "http://localhost:3002", // Vite 추가 포트 (포트 충돌 시)
                "http://localhost:5173", // Vite 대체 포트
                "http://localhost:5174" // Vite 추가 포트
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // 인증 정보 포함 허용 (JWT 토큰 전송을 위해 필요)
        configuration.setAllowCredentials(true);

        // 응답에서 노출할 헤더 (프론트엔드에서 X-Request-ID 접근 허용)
        configuration.setExposedHeaders(Arrays.asList("X-Request-ID"));

        // Preflight 요청 캐시 시간 (초)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

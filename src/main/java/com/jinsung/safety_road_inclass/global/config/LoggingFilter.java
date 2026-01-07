package com.jinsung.safety_road_inclass.global.config;

import com.github.f4b6a3.ulid.UlidCreator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 요청 로깅 필터
 * 
 * 모든 HTTP 요청에 대해 Request ID를 추출/생성하여 MDC에 저장합니다.
 * - 클라이언트가 X-Request-ID 헤더로 ID를 전달하면 해당 값 사용
 * - 헤더가 없으면 서버에서 ULID 생성
 * - 응답 헤더에도 동일한 ID를 설정하여 클라이언트가 확인 가능
 * 
 * ULID(Universally Unique Lexicographically Sortable Identifier)는
 * 시간 정보를 포함하므로 로그 정렬에 유리합니다.
 * 
 * API 메트릭 로깅:
 * - METRIC:API_CALL 형식으로 요청/응답 시간을 기록합니다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String MDC_REQUEST_ID_KEY = "requestId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String requestId = request.getHeader(REQUEST_ID_HEADER);

        // 헤더에 Request ID가 없으면 서버에서 ULID 생성
        if (requestId == null || requestId.isBlank()) {
            requestId = UlidCreator.getUlid().toString();
            log.debug("Request ID not found in header, generated new ULID: {}", requestId);
        } else {
            log.debug("Using Request ID from header: {}", requestId);
        }

        // API 호출 시간 측정 시작
        long startTime = System.currentTimeMillis();

        try {
            // MDC에 requestId 저장 - 모든 로그에 자동으로 포함됨
            MDC.put(MDC_REQUEST_ID_KEY, requestId);

            // 응답 헤더에도 동일한 ID 설정
            response.setHeader(REQUEST_ID_HEADER, requestId);

            // 요청 처리 계속
            filterChain.doFilter(request, response);

        } finally {
            // API 응답 시간 계산
            long duration = System.currentTimeMillis() - startTime;

            // API 엔드포인트 경로 (/api/로 시작하는 경우에만 메트릭 로깅)
            String path = request.getRequestURI();
            if (path != null && path.startsWith("/api")) {
                log.info("METRIC:API_CALL requestId={} method={} path={} status={} durationMs={}",
                        requestId,
                        request.getMethod(),
                        path,
                        response.getStatus(),
                        duration);
            }

            // 요청 처리 완료 후 반드시 MDC 정리
            MDC.clear();
        }
    }
}

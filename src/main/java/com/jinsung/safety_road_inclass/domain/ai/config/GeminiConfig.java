package com.jinsung.safety_road_inclass.domain.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Gemini API 설정
 * - API Key, URL, Timeout 관리
 */
@Configuration
@ConfigurationProperties(prefix = "gemini.api")
@Getter
@Setter
public class GeminiConfig {
    
    /**
     * Gemini API Key
     */
    private String key;
    
    /**
     * Gemini API URL
     */
    private String url;
    
    /**
     * API 요청 타임아웃 (ms)
     */
    private int timeout = 30000;
    
    /**
     * Gemini API 호출용 RestTemplate Bean
     */
    @Bean(name = "geminiRestTemplate")
    public RestTemplate geminiRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        return new RestTemplate(factory);
    }
    
    /**
     * API Key가 포함된 전체 URL 반환
     */
    public String getFullUrl() {
        return url + "?key=" + key;
    }
}


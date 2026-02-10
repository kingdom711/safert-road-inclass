package com.jinsung.safety_road_inclass.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Supabase Storage 설정
 */
@Configuration
@Profile("prod")
@ConfigurationProperties(prefix = "supabase.storage")
@Getter
@Setter
public class SupabaseStorageProperties {

    /**
     * Supabase 프로젝트 URL (예: https://xxxxx.supabase.co)
     */
    private String url;

    /**
     * Supabase service_role 키 (서버 사이드 업로드용)
     */
    private String key;

    /**
     * Storage 버킷 이름
     */
    private String bucket = "safety-road-photos";
}

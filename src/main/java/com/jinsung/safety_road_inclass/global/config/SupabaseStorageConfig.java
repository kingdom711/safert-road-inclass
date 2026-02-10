package com.jinsung.safety_road_inclass.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Supabase Storage용 RestTemplate 설정
 */
@Configuration
@Profile("prod")
public class SupabaseStorageConfig {

    @Bean(name = "supabaseRestTemplate")
    public RestTemplate supabaseRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(30_000);
        return new RestTemplate(factory);
    }
}

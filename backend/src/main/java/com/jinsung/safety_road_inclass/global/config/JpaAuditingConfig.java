package com.jinsung.safety_road_inclass.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing Configuration
 * - @CreatedDate, @LastModifiedDate 자동 설정
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}


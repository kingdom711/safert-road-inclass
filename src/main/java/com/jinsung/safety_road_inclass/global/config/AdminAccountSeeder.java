package com.jinsung.safety_road_inclass.global.config;

import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the legacy admin account used for feature verification only.
 *
 * This account is intentionally separate from the operational project-admin
 * role and should not be treated as the primary production admin account.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(999) // DataSeeder(1000)보다 먼저 실행
public class AdminAccountSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            if (userRepository.existsByUsername("admin")) {
                log.info("Legacy admin verification account already exists.");
                return;
            }

            User admin = User.builder()
                    .username("admin")
                    .password("123123")
                    .role(Role.ROLE_ADMIN)
                    .name("관리자")
                    .email("admin@safetyroad.com")
                    .isVerified(true)
                    .build();
            admin.encodePassword(passwordEncoder);
            userRepository.save(admin);

            log.info("Legacy admin verification account created (ROLE_ADMIN).");
        } catch (Exception e) {
            log.error("Failed to create legacy admin verification account: {}", e.getMessage(), e);
        }
    }
}

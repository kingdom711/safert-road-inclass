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
 * AdminAccountSeeder - 관리자 계정 초기화
 *
 * 모든 환경(로컬/배포)에서 실행됩니다.
 * admin 계정이 없으면 자동으로 생성합니다.
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
                log.info("admin 계정이 이미 존재합니다.");
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

            log.info("=== admin 계정 생성 완료 (ROLE_ADMIN, 모든 역할 기능 사용 가능) ===");
        } catch (Exception e) {
            log.error("admin 계정 생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}

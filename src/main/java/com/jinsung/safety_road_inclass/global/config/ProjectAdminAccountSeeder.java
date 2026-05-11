package com.jinsung.safety_road_inclass.global.config;

import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(998)
public class ProjectAdminAccountSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${project-admin.username:}")
    private String username;

    @Value("${project-admin.password:}")
    private String password;

    @Override
    public void run(String... args) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.info("Project admin account seeding skipped: project-admin.username/password not configured");
            return;
        }

        if (userRepository.existsByUsername(username)) {
            log.info("Project admin account already exists: {}", username);
            return;
        }

        User projectAdmin = User.builder()
                .username(username)
                .password(password)
                .role(Role.ROLE_PROJECT_ADMIN)
                .name("Project Admin")
                .email(username)
                .isVerified(true)
                .build();
        projectAdmin.encodePassword(passwordEncoder);
        userRepository.save(projectAdmin);

        log.info("Project admin account created: {}", username);
    }
}

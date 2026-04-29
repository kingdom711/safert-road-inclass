package com.jinsung.safety_road_inclass.domain.auth.service;

import com.jinsung.safety_road_inclass.domain.auth.dto.LoginRequest;
import com.jinsung.safety_road_inclass.domain.auth.dto.LoginResponse;
import com.jinsung.safety_road_inclass.domain.auth.dto.SignupRequest;
import com.jinsung.safety_road_inclass.domain.auth.dto.TokenRefreshRequest;
import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService - 인증 서비스
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 회원가입
     */
    @Transactional
    public LoginResponse signup(SignupRequest request) {
        // 1. 이메일(username) 중복 체크
        if (userRepository.existsByUsername(request.getEmail())) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // 2. User 생성 (이메일을 username으로 사용, 기본 역할: WORKER)
        User user = User.builder()
                .username(request.getEmail())
                .password(request.getPassword())
                .role(Role.ROLE_WORKER)
                .name(request.getName())
                .email(request.getEmail())
                .build();
        user.encodePassword(passwordEncoder);
        userRepository.save(user);

        // 3. 토큰 발급 (회원가입 즉시 로그인)
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("회원가입 성공: userId={}, username={}", user.getId(), user.getUsername());

        return LoginResponse.of(accessToken, refreshToken, user);
    }

    /**
     * 로그인
     */
    public LoginResponse login(LoginRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 비밀번호 검증
        if (!user.matchesPassword(passwordEncoder, request.getPassword())) {
            log.warn("로그인 실패 - 비밀번호 불일치: username={}", request.getUsername());
            throw new CustomException(ErrorCode.AUTH_INVALID_PASSWORD);
        }

        // 3. 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("로그인 성공: userId={}, username={}, role={}", 
                 user.getId(), user.getUsername(), user.getRole());

        return LoginResponse.of(accessToken, refreshToken, user);
    }

    /**
     * 토큰 갱신
     */
    public LoginResponse refreshToken(TokenRefreshRequest request) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
        }

        // 2. 토큰에서 사용자 정보 추출
        String username = jwtTokenProvider.getUsername(request.getRefreshToken());

        // 3. 사용자 조회
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 4. 새 토큰 발급
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("토큰 갱신 성공: userId={}, username={}", user.getId(), user.getUsername());

        return LoginResponse.of(newAccessToken, newRefreshToken, user);
    }

    /**
     * 현재 로그인된 사용자 조회
     */
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * ID로 사용자 조회
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 현재 사용자 응답 DTO 조회
     */
    public LoginResponse.UserInfo getUserInfoById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return LoginResponse.UserInfo.from(user);
    }
}


package com.jinsung.safety_road_inclass.domain.auth.service;

import com.jinsung.safety_road_inclass.domain.auth.dto.LoginRequest;
import com.jinsung.safety_road_inclass.domain.auth.dto.LoginResponse;
import com.jinsung.safety_road_inclass.domain.auth.dto.PasswordResetApprovalResponse;
import com.jinsung.safety_road_inclass.domain.auth.dto.PasswordResetConfirmRequest;
import com.jinsung.safety_road_inclass.domain.auth.dto.PasswordResetRequest;
import com.jinsung.safety_road_inclass.domain.auth.dto.SignupRequest;
import com.jinsung.safety_road_inclass.domain.auth.dto.TokenRefreshRequest;
import com.jinsung.safety_road_inclass.domain.auth.entity.PasswordResetApprovalRequest;
import com.jinsung.safety_road_inclass.domain.auth.entity.PasswordResetApprovalStatus;
import com.jinsung.safety_road_inclass.domain.auth.entity.PasswordResetCode;
import com.jinsung.safety_road_inclass.domain.auth.entity.Role;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.PasswordResetApprovalRequestRepository;
import com.jinsung.safety_road_inclass.domain.auth.repository.PasswordResetCodeRepository;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

/**
 * AuthService - 인증 서비스
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetApprovalRequestRepository passwordResetApprovalRequestRepository;
    private final PasswordResetCodeRepository passwordResetCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordResetMailService passwordResetMailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.password-reset.code-secret:${jwt.secret}}")
    private String passwordResetCodeSecret;

    @Value("${app.password-reset.show-code-in-response:true}")
    private boolean showPasswordResetCodeInResponse;

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

    @Transactional
    public void requestPasswordReset(PasswordResetRequest request) {
        String email = request.getEmail().trim();

        userRepository.findByUsernameIgnoreCase(email)
                .or(() -> userRepository.findByEmailIgnoreCase(email))
                .map(user -> {
                    LocalDateTime now = LocalDateTime.now();
                    passwordResetApprovalRequestRepository.findAllByUserAndStatus(user, PasswordResetApprovalStatus.PENDING)
                            .forEach(pending -> pending.reject(null, "새 비밀번호 변경 요청으로 이전 요청이 취소되었습니다.", now));

                    PasswordResetApprovalRequest resetRequest = PasswordResetApprovalRequest.builder()
                            .user(user)
                            .requestedEmail(email)
                            .encodedNewPassword(passwordEncoder.encode(request.getNewPassword()))
                            .build();
                    passwordResetApprovalRequestRepository.save(resetRequest);
                    log.info("비밀번호 재설정 승인 요청 생성: userId={}, username={}", user.getId(), user.getUsername());
                    return resetRequest;
                });
    }

    @Transactional
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        String email = request.getEmail().trim();
        User user = userRepository.findByUsernameIgnoreCase(email)
                .or(() -> userRepository.findByEmailIgnoreCase(email))
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT, "인증 코드가 올바르지 않거나 만료되었습니다."));

        PasswordResetCode resetCode = passwordResetCodeRepository.findFirstByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_INPUT, "인증 코드가 올바르지 않거나 만료되었습니다."));

        LocalDateTime now = LocalDateTime.now();
        if (!resetCode.isUsable(now)) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "인증 코드가 올바르지 않거나 만료되었습니다.");
        }

        if (!resetCode.matches(hashResetCode(user, request.getCode()))) {
            resetCode.recordFailedAttempt();
            throw new CustomException(ErrorCode.INVALID_INPUT, "인증 코드가 올바르지 않거나 만료되었습니다.");
        }

        user.changePassword(passwordEncoder, request.getNewPassword());
        resetCode.markUsed(now);

        log.info("비밀번호 재설정 완료: userId={}, username={}", user.getId(), user.getUsername());
    }

    public List<PasswordResetApprovalResponse> getPasswordResetRequests(boolean pendingOnly) {
        List<PasswordResetApprovalRequest> requests = pendingOnly
                ? passwordResetApprovalRequestRepository.findAllByStatusOrderByCreatedAtDesc(PasswordResetApprovalStatus.PENDING)
                : passwordResetApprovalRequestRepository.findAllByOrderByCreatedAtDesc();

        return requests.stream()
                .map(PasswordResetApprovalResponse::from)
                .toList();
    }

    @Transactional
    public PasswordResetApprovalResponse approvePasswordReset(Long requestId, Long adminId) {
        PasswordResetApprovalRequest request = passwordResetApprovalRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "비밀번호 변경 요청을 찾을 수 없습니다."));
        if (!request.isPending()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "이미 처리된 요청입니다.");
        }

        request.getUser().replaceEncodedPassword(request.getEncodedNewPassword());
        request.approve(adminId, LocalDateTime.now());
        log.info("비밀번호 재설정 승인: requestId={}, userId={}, adminId={}", requestId, request.getUser().getId(), adminId);
        return PasswordResetApprovalResponse.from(request);
    }

    @Transactional
    public PasswordResetApprovalResponse rejectPasswordReset(Long requestId, Long adminId, String reason) {
        PasswordResetApprovalRequest request = passwordResetApprovalRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND, "비밀번호 변경 요청을 찾을 수 없습니다."));
        if (!request.isPending()) {
            throw new CustomException(ErrorCode.INVALID_INPUT, "이미 처리된 요청입니다.");
        }

        request.reject(adminId, reason == null || reason.isBlank() ? "관리자가 요청을 거절했습니다." : reason.trim(), LocalDateTime.now());
        log.info("비밀번호 재설정 거절: requestId={}, userId={}, adminId={}", requestId, request.getUser().getId(), adminId);
        return PasswordResetApprovalResponse.from(request);
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

    private String generateCode() {
        return "%06d".formatted(secureRandom.nextInt(1_000_000));
    }

    private String hashResetCode(User user, String rawCode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String material = user.getId() + ":" + rawCode + ":" + passwordResetCodeSecret;
            return HexFormat.of().formatHex(digest.digest(material.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}


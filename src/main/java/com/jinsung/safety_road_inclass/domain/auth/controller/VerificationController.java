package com.jinsung.safety_road_inclass.domain.auth.controller;

import com.jinsung.safety_road_inclass.domain.auth.service.JwtTokenProvider.CustomUserPrincipal;
import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.domain.auth.repository.UserRepository;
import com.jinsung.safety_road_inclass.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class VerificationController {

    private final UserRepository userRepository;
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    @GetMapping("/verification-status")
    public ApiResponse<Map<String, Object>> getVerificationStatus(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        User user = userRepository.findByUsername(principal.username())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ApiResponse.success(Map.of(
                "isVerified", user.isVerified(),
                "phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : ""));
    }

    @PostMapping("/verify/request")
    public ApiResponse<String> requestVerification(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        // Mock: Generate random 6-digit code
        String code = String.format("%06d", new Random().nextInt(1000000));
        verificationCodes.put(phoneNumber, code);

        // In production, send SMS here. For mock, just return success.
        // We log the code for debugging/demo purposes (or user finds it in
        // console/response in real dev env)
        System.out.println("Verification Code for " + phoneNumber + ": " + code);

        return ApiResponse.success("Verification code sent (Mock: " + code + ")");
    }

    @PostMapping("/verify/confirm")
    @Transactional
    public ApiResponse<String> confirmVerification(@AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        String code = request.get("code");

        if (!verificationCodes.containsKey(phoneNumber) || !verificationCodes.get(phoneNumber).equals(code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        User user = userRepository.findByUsername(principal.username())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userRepository.existsByPhoneNumber(phoneNumber) && !user.getPhoneNumber().equals(phoneNumber)) {
            throw new IllegalArgumentException("Phone number already used by another account");
        }

        user.verify(phoneNumber);
        verificationCodes.remove(phoneNumber);

        return ApiResponse.success("Verification successful");
    }
}

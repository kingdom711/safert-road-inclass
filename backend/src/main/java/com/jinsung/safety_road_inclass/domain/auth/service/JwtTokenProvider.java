package com.jinsung.safety_road_inclass.domain.auth.service;

import com.jinsung.safety_road_inclass.domain.auth.entity.User;
import com.jinsung.safety_road_inclass.global.error.CustomException;
import com.jinsung.safety_road_inclass.global.error.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;

/**
 * JwtTokenProvider - JWT 토큰 생성 및 검증
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token 생성
     */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
            .subject(user.getUsername())
            .claim("userId", user.getId())
            .claim("role", user.getRole().name())
            .claim("name", user.getName())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact();
    }

    /**
     * Refresh Token 생성
     */
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
            .subject(user.getUsername())
            .claim("userId", user.getId())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact();
    }

    /**
     * 토큰에서 Authentication 객체 추출
     */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        
        String username = claims.getSubject();
        String role = claims.get("role", String.class);
        Long userId = claims.get("userId", Long.class);

        // UserDetails 대신 간단한 Principal 사용
        CustomUserPrincipal principal = new CustomUserPrincipal(userId, username, role);
        
        return new UsernamePasswordAuthenticationToken(
            principal,
            null,
            Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    /**
     * 토큰에서 username 추출
     */
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰: {}", e.getMessage());
            throw new CustomException(ErrorCode.AUTH_EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
            throw new CustomException(ErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    /**
     * 토큰 파싱
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    /**
     * CustomUserPrincipal - 인증된 사용자 정보
     */
    public record CustomUserPrincipal(Long userId, String username, String role) {
    }
}


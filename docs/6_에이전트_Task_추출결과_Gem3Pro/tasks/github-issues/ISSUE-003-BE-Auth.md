---
title: "[BE] Auth 도메인: 인증/인가 및 JWT 구현"
labels: ["backend", "security", "feature"]
assignees: []
---

## 1. 배경 (Background)
사용자 식별 및 역할 기반 접근 제어(RBAC)를 위해 JWT 기반 인증 시스템을 구축해야 합니다.
관련 문서: `REQ-FUNC-01`, `REQ-NF-001`

## 2. 상세 작업 (Tasks)
- [ ] **Domain Modeling (`domain/auth`)**
    - [ ] `User` Entity 설계 (username, password, role, name)
    - [ ] `UserRepository` 생성
- [ ] **JWT Implementation**
    - [ ] `JwtTokenProvider`: Access/Refresh Token 생성 및 검증 로직
    - [ ] `JwtAuthenticationFilter`: 요청 헤더에서 토큰 추출 및 SecurityContext 설정
- [ ] **Service Layer**
    - [ ] `AuthService`: 로그인(비밀번호 검증), 회원가입(옵션), 토큰 재발급
- [ ] **API Implementation**
    - [ ] `POST /api/auth/login`: 로그인 및 토큰 반환
    - [ ] `POST /api/auth/refresh`: 토큰 갱신

## 3. 완료 조건 (Acceptance Criteria)
- [ ] 유효한 ID/PW로 로그인 시 Access Token이 발급되어야 한다.
- [ ] 인증되지 않은 사용자가 보호된 API 접근 시 401 에러가 발생해야 한다.
- [ ] `ROLE_WORKER` 권한을 가진 토큰으로 관리자 API 접근 시 403 에러가 발생해야 한다.


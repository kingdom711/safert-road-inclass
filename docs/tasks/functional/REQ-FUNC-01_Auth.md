# REQ-FUNC-01: 사용자 인증 및 권한 관리

## 1. 개요
- **목적**: JWT 기반의 인증 및 RBAC(Role Based Access Control) 구현.
- **SRS 섹션**: 3.1 F-01
- **구성 요소**:
    - BE: 로그인 API, 토큰 발급/검증 필터, Security Config
    - FE: 로그인 페이지, 토큰 저장/관리, Axios Interceptor
    - DB: User 테이블

## 2. Task 상세 내용 (Machine Readable)

```yaml
task_id: "REQ-FUNC-01-AUTH"
title: "JWT 기반 인증 및 역할별 접근 제어(RBAC) 구현"
summary: >
  Spring Security와 JWT를 사용하여 로그인(토큰 발급), 인증 필터(토큰 검증),
  역할(WORKER, SUPERVISOR, SAFETY_MANAGER) 기반 API 접근 제어를 구현한다.
  프론트엔드 로그인 연동을 포함한다.
type: "functional"
epic: "EPIC_1_CHECKLIST"
req_ids: ["F-01"]
component: ["backend", "frontend", "database"]

context:
  apis:
    - "POST /api/auth/login"
    - "POST /api/auth/refresh"
  entities: ["User"]
  roles: ["ROLE_WORKER", "ROLE_SUPERVISOR", "ROLE_SAFETY_MANAGER"]

inputs:
  description: "로그인 요청 (username, password)"
  fields:
    - name: "username"
      type: "string"
    - name: "password"
      type: "string"

outputs:
  description: "Access Token, Refresh Token, User Info"

steps_hint:
  - "DB: User 테이블 및 초기 데이터(테스트 계정) 생성 (Flyway/DDL)"
  - "BE: Spring Security SecurityFilterChain 설정 (CSRF, CORS, SessionStateless)"
  - "BE: JwtTokenProvider 구현 (생성, 검증, 파싱)"
  - "BE: LoginService 구현 (비밀번호 검증, 토큰 발급)"
  - "FE: 로그인 페이지 UI 및 API 연동"
  - "FE: LocalStorage 토큰 저장 및 Axios Interceptor (Header 주입) 설정"

preconditions:
  - "DB 스키마 관리 환경(Flyway 등)이 설정되어 있어야 한다."
  - "프론트엔드 프로젝트가 생성되어 있어야 한다."

postconditions:
  - "유효한 계정으로 로그인 시 JWT를 발급받는다."
  - "보호된 API(/api/checklists 등) 호출 시 토큰 없이는 401, 권한 부족 시 403 응답을 받는다."

dependencies: []
parallelizable: false
estimated_effort: "M"
priority: "Must"
agent_profile: ["backend", "frontend"]
required_tools:
  languages: ["Java", "TypeScript"]
  frameworks: ["Spring Boot", "React"]
```


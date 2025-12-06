---
title: "[BE] Global 패키지 및 공통 설정 구현"
labels: ["backend", "refactor"]
assignees: []
---

## 1. 배경 (Background)
모든 도메인에서 공통적으로 사용하는 예외 처리, 응답 포맷, 보안 설정, 유틸리티 클래스를 `global` 패키지에 구현하여 코드 중복을 방지하고 일관성을 유지합니다.

## 2. 상세 작업 (Tasks)
- [ ] **Config 설정**
    - [ ] `SecurityConfig`: CSRF 비활성화, CORS 설정, Session Stateless 설정
    - [ ] `SwaggerConfig`: API 문서화 도구(OpenAPI) 설정
    - [ ] `JpaAuditingConfig`: 엔티티 생성/수정 시간 자동화
- [ ] **Common 모듈 구현**
    - [ ] `ApiResponse<T>`: 성공/실패 응답을 감싸는 공통 래퍼 클래스
    - [ ] `BaseTimeEntity`: `@CreatedDate`, `@LastModifiedDate` 포함
- [ ] **Error Handling**
    - [ ] `ErrorCode` (Enum): 전역 에러 코드 정의 (예: INVALID_INPUT, AUTH_FAILED)
    - [ ] `GlobalExceptionHandler` (@ControllerAdvice): 예외를 잡아 표준 응답으로 변환

## 3. 완료 조건 (Acceptance Criteria)
- [ ] 잘못된 API 경로 호출 시 표준 JSON 에러 응답이 반환되어야 한다.
- [ ] 모든 API 응답은 `ApiResponse` 형태여야 한다.


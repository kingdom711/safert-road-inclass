---
title: "[NF] 보안 감사(Audit) 및 로깅 시스템"
labels: ["backend", "security", "non-functional"]
assignees: []
---

## 1. 배경 (Background)
시스템의 안전성과 추적성을 확보하기 위해 주요 행위에 대한 로그를 남기고 보안 설정을 강화합니다.
관련 문서: `REQ-NF-001`, `REQ-NF-002`

## 2. 상세 작업 (Tasks)
- [ ] **Auditing**
    - [ ] JPA Auditing 활성화: 생성자(`@CreatedBy`), 수정자(`@LastModifiedBy`) 자동 기록
    - [ ] SecurityContextHolder 연동
- [ ] **Logging**
    - [ ] Logback 설정: 콘솔 및 파일 로그 분리
    - [ ] AOP 기반 Request/Response 로깅 (성능 모니터링)
- [ ] **Security Hardening**
    - [ ] Rate Limiting (Bucket4j 등): 과도한 요청 방지
    - [ ] XSS 방지 필터

## 3. 완료 조건 (Acceptance Criteria)
- [ ] DB 데이터 생성 시 `created_by` 컬럼에 로그인한 사용자 ID가 저장되어야 한다.
- [ ] 에러 발생 시 로그 파일에 StackTrace가 기록되어야 한다.


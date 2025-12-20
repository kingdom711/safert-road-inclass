# 🔗 백엔드 통합 개발 가이드

> **통합 문서** - 백엔드 개발에 필요한 모든 정보를 한 곳에서 확인할 수 있습니다.

---

## 📚 관련 문서 바로가기

| 문서 | 설명 | 주요 내용 |
|------|------|----------|
| [database.md](./database.md) | DB 설정 가이드 | H2/MySQL 프로파일 설정 |
| [tasks/functional/REQ-FUNC-06_AI_Analysis.md](./tasks/functional/REQ-FUNC-06_AI_Analysis.md) | GEMS AI API 상세 | Request/Response 명세 |
| [tasks/github-issues/ISSUE-011-BE-AI.md](./tasks/github-issues/ISSUE-011-BE-AI.md) | AI 구현 이슈 | 구현 상태 및 파일 목록 |

---

## 🎯 Quick Start - 백엔드 개발 시작하기

### 1️⃣ 현재 구현된 API 엔드포인트

```
# 인증 API (/api/v1/auth)
POST /api/v1/auth/login           - 로그인 ✅
POST /api/v1/auth/refresh         - 토큰 갱신 ✅
GET  /api/v1/auth/me              - 내 정보 조회 ✅

# 체크리스트 API (/api/v1/checklists)
POST /api/v1/checklists           - 체크리스트 제출 ✅
GET  /api/v1/checklists/my        - 내 체크리스트 목록 ✅
GET  /api/v1/checklists/{id}      - 체크리스트 상세 ✅
GET  /api/v1/checklists/status/{status} - 상태별 조회 ✅
GET  /api/v1/checklists/with-risk - 위험 항목 있는 체크리스트 ✅

# 템플릿 API (/api/v1/templates)
GET  /api/v1/templates            - 템플릿 목록 ✅
GET  /api/v1/templates/{id}       - 템플릿 상세 ✅

# 검토 API (/api/v1/reviews)
POST /api/v1/reviews/{checklistId}         - 체크리스트 검토 ✅
GET  /api/v1/reviews/{checklistId}/history - 검토 이력 ✅
GET  /api/v1/reviews/recent                - 최근 검토 이력 ✅

# 위험성 평가 API (/api/v1/risks)
GET  /api/v1/risks/pending                 - 평가 대상 조회 ✅
POST /api/v1/risks/{itemId}/assess         - 위험성 평가 등록 ✅
GET  /api/v1/risks/{assessmentId}          - 평가 상세 조회 ✅
GET  /api/v1/risks/high-risk               - 고위험 항목 조회 ✅
GET  /api/v1/risks/level/{level}           - 위험 레벨별 조회 ✅
GET  /api/v1/risks/countermeasures/incomplete - 미완료 대책 ✅
GET  /api/v1/risks/countermeasures/overdue    - 기한 초과 대책 ✅
PATCH /api/v1/risks/countermeasures/{id}/complete - 대책 완료 ✅

# GEMS AI 분석 API (/api/v1/business-plan) ⭐
POST /api/v1/business-plan/generate - 위험 분석 요청 ✅
GET  /api/v1/business-plan/history  - 분석 기록 조회 ✅
GET  /api/v1/business-plan/health   - 서비스 상태 ✅

# 기타
GET  /api/v1/health               - 서버 상태 확인 ✅
GET  /api/v1/files/{filename}     - 파일 다운로드 ✅
```

---

### 2️⃣ GEMS AI 분석 API (핵심 기능) ⭐

#### Request

```json
{
  "inputType": "TEXT",
  "inputText": "건설 현장 2층 비계 작업 중 안전난간이 심하게 흔들리고 있습니다...",
  "photoId": null,
  "context": {
    "workType": "construction",
    "location": "2층 비계",
    "workerCount": 3,
    "currentTask": "철골 용접 작업"
  }
}
```

#### Response

```json
{
  "success": true,
  "data": {
    "riskFactor": "고소 작업 중 안전대 미체결",
    "remediationSteps": [
      "즉시 작업을 중단하고 안전한 장소로 이동하십시오.",
      "안전대 및 부속품의 상태를 점검하십시오.",
      "안전대 체결 후 2인 1조로 작업을 재개하십시오."
    ],
    "referenceCode": "KOSHA-G-2023-01",
    "actionRecordId": "550e8400-e29b-41d4-a716-446655440000",
    "riskLevel": "HIGH",
    "analysisId": "analysis-2024-12-17-001",
    "analyzedAt": "2024-12-17T10:30:00.000Z"
  }
}
```

---

## 📂 백엔드 프로젝트 구조

```
backend/src/main/java/com/jinsung/safety_road_inclass/
├── SafetyRoadInclassApplication.java
├── domain/
│   ├── ai/                          # GEMS AI 분석 ⭐
│   │   ├── controller/
│   │   │   ├── AiController.java           # 레거시 API
│   │   │   └── BusinessPlanController.java # 프론트엔드 연동용 ⭐
│   │   ├── dto/
│   │   │   ├── BusinessPlanRequest.java    # 프론트엔드 형식 ⭐
│   │   │   └── BusinessPlanResponse.java   # 프론트엔드 형식 ⭐
│   │   └── service/
│   │       └── BusinessPlanService.java    # KOSHA Mock ⭐
│   ├── auth/                        # 인증
│   ├── checklist/                   # 체크리스트
│   ├── template/                    # 템플릿
│   ├── review/                      # 검토/승인
│   ├── risk/                        # 위험성 평가
│   └── notification/                # 알림 (예정)
└── global/
    ├── common/                      # 공통 응답
    ├── config/                      # 설정 (Security, CORS, etc.)
    ├── controller/                  # 파일 컨트롤러
    ├── error/                       # 예외 처리
    └── service/                     # 스토리지 서비스
```

---

## 🔧 Spring Boot 설정

### 개발 환경 (H2 인메모리 DB)

```properties
# application.properties
spring.profiles.active=dev

# application-dev.properties
spring.datasource.url=jdbc:h2:mem:safetyroad;DB_CLOSE_DELAY=-1;MODE=MySQL
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.jpa.hibernate.ddl-auto=create-drop
```

### 프로덕션 환경 (MySQL)

```properties
# application.properties
spring.profiles.active=mysql

# application-mysql.properties  
spring.datasource.url=jdbc:mysql://localhost:3306/safetyroad
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

---

## 🛡️ CORS 설정 (SecurityConfig.java)

현재 개발 환경에서 허용된 엔드포인트:

```java
.requestMatchers("/api/v1/auth/**").permitAll()
.requestMatchers("/api/v1/health").permitAll()
.requestMatchers("/api/v1/ai/**").permitAll()
.requestMatchers("/api/v1/business-plan/**").permitAll()  // GEMS AI ⭐
.requestMatchers("/h2-console/**").permitAll()
.requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
```

---

## 📋 Mock 데이터 (GEMS AI)

입력 텍스트에 따라 다른 KOSHA 코드 기반 응답을 반환합니다:

| 키워드 | 위험 요인 | KOSHA 코드 | 위험도 |
|--------|----------|------------|--------|
| 안전대, 추락, 고소 | 고소 작업 중 안전대 미체결 | KOSHA-G-2023-01 | HIGH |
| 화기, 용접, 가연 | 가연성 물질 주변 화기 작업 | KOSHA-M-2023-05 | CRITICAL |
| 밀폐, 산소, 질식 | 밀폐공간 산소 농도 미확인 | KOSHA-S-2023-03 | CRITICAL |
| 기타 | 안전난간 불안정 및 추락 위험 | KOSHA-C-2023-08 | HIGH |

---

## ✅ 백엔드 구현 체크리스트

### 🔷 완료된 기능
- [x] Spring Boot 프로젝트 구성
- [x] H2/MySQL 데이터베이스 설정
- [x] JWT 인증 시스템
- [x] 인증 API (login, refresh, me)
- [x] 체크리스트 API (CRUD, 상태별 조회)
- [x] 템플릿 API (목록, 상세)
- [x] 검토/승인 API
- [x] 위험성 평가 API
- [x] **GEMS AI API (Mock)** ⭐

### 🔷 추후 구현 예정
- [ ] 회원가입 API
- [ ] 로그아웃 API
- [ ] Gemini API 실제 연동
- [ ] 분석 로그 DB 저장
- [ ] 알림 API

---

## 🔗 테스트 URL

| 서비스 | URL |
|--------|-----|
| API 서버 | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |
| Health Check | http://localhost:8080/api/v1/health |

---

## 📊 KOSHA 참조 코드 목록

| 코드 | 분류 | 설명 |
|------|------|------|
| `KOSHA-G-2023-01` | 고소작업 | 안전대 관련 기준 |
| `KOSHA-M-2023-05` | 화기작업 | 화재 예방 기준 |
| `KOSHA-P-2023-12` | 보호구 | 개인보호구 착용 기준 |
| `KOSHA-C-2023-08` | 가설구조 | 비계 및 거푸집 기준 |
| `KOSHA-S-2023-03` | 밀폐공간 | 밀폐공간 작업 기준 |
| `KOSHA-E-2023-07` | 전기작업 | 전기 안전 기준 |
| `KOSHA-L-2023-11` | 양중작업 | 크레인 및 양중기 기준 |
| `KOSHA-F-2023-04` | 화재예방 | 용접 화재 감시 기준 |

---

*문서 업데이트: 2025-12-17*
*백엔드-프론트엔드 연동 버전: 2.0.0*

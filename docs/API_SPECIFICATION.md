# Safety Road 백엔드 API 명세

> **작성일**: 2026-01-08  
> **Base URL**: `http://localhost:8080`  
> **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`  
> **API Docs**: `http://localhost:8080/api-docs`

---

## 목차

1. [인증 API (Auth)](#1-인증-api-auth)
2. [GEMS AI 분석 API (Business Plan)](#2-gems-ai-분석-api-business-plan)
3. [AI 분석 API (Legacy)](#3-ai-분석-api-legacy)
4. [체크리스트 API (Checklist)](#4-체크리스트-api-checklist)
5. [템플릿 API (Template)](#5-템플릿-api-template)
6. [위험성 평가 API (Risk)](#6-위험성-평가-api-risk)
7. [검토 API (Review)](#7-검토-api-review)
8. [파일 API (File)](#8-파일-api-file)
9. [헬스체크 API (Health)](#9-헬스체크-api-health)
10. [테스트용 계정](#10-테스트용-사용자-계정)

---

## 1. 인증 API (Auth)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `POST` | `/api/v1/auth/login` | 로그인 (JWT 토큰 발급) | ❌ |
| `POST` | `/api/v1/auth/refresh` | 토큰 갱신 | ❌ |
| `GET` | `/api/v1/auth/me` | 내 정보 조회 | ✅ |

### 요청/응답 예시

#### 로그인

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "worker1",
  "password": "worker123"
}
```

#### 응답

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "worker1",
      "name": "작업자1",
      "role": "ROLE_WORKER"
    }
  },
  "error": null
}
```

#### 토큰 갱신

```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 내 정보 조회

```http
GET /api/v1/auth/me
Authorization: Bearer {accessToken}
```

---

## 2. GEMS AI 분석 API (Business Plan)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `POST` | `/api/v1/business-plan/generate` | 위험 상황 AI 분석 | ❌ |
| `GET` | `/api/v1/business-plan/history` | 분석 기록 조회 | ❌ |
| `GET` | `/api/v1/business-plan/health` | 서비스 상태 확인 | ❌ |

### 요청/응답 예시

#### 위험 상황 분석 요청

```http
POST /api/v1/business-plan/generate
Content-Type: application/json

{
  "inputType": "TEXT",
  "inputText": "건설 현장 2층 비계 작업 중 안전난간이 심하게 흔들리고 있습니다. 작업자 3명이 해당 구역에서 철골 용접 작업을 진행 중이며, 안전대 체결 상태가 불량하여 추락 사고 위험이 매우 높은 상황입니다.",
  "photoId": null,
  "context": {
    "workType": "construction",
    "location": "2층 비계",
    "workerCount": 3,
    "currentTask": "철골 용접 작업"
  }
}
```

#### 응답

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
    "analysisId": "analysis-2025-01-08-abc12345",
    "analyzedAt": "2025-01-08T10:30:00.000Z"
  },
  "error": null
}
```

### inputType 값

- `TEXT` - 텍스트 기반 분석
- `PHOTO` - 사진 기반 분석

### riskLevel 값

- `LOW` - 저위험
- `MEDIUM` - 중위험
- `HIGH` - 고위험
- `CRITICAL` - 심각

---

## 3. AI 분석 API (Legacy)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `POST` | `/api/v1/ai/analyze` | 텍스트 기반 위험 분석 | ❌ |
| `POST` | `/api/v1/ai/analyze-photo` | 이미지 기반 위험 분석 (Multipart) | ❌ |
| `GET` | `/api/v1/ai/health` | AI 서비스 상태 확인 | ❌ |

### 텍스트 분석 요청

```http
POST /api/v1/ai/analyze
Content-Type: application/json

{
  "checklistId": 1,
  "content": "고소 작업 시 안전대 미착용 상태로 작업 중"
}
```

### 이미지 분석 요청

```http
POST /api/v1/ai/analyze-photo
Content-Type: multipart/form-data

photo: [이미지 파일]
```

---

## 4. 체크리스트 API (Checklist)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `POST` | `/api/v1/checklists` | 체크리스트 제출 (multipart) | ✅ |
| `GET` | `/api/v1/checklists/my` | 내 체크리스트 목록 조회 (페이징) | ✅ |
| `GET` | `/api/v1/checklists/{checklistId}` | 체크리스트 상세 조회 | ✅ |
| `GET` | `/api/v1/checklists/status/{status}` | 상태별 체크리스트 조회 | ✅ |
| `GET` | `/api/v1/checklists/with-risk` | 위험 항목 포함 체크리스트 조회 | ✅ |

### 상태 값 (ChecklistStatus)

| 값 | 설명 |
|----|------|
| `DRAFT` | 임시저장 |
| `SUBMITTED` | 제출됨 |
| `APPROVED` | 승인됨 |
| `REJECTED` | 반려됨 |

### 체크리스트 제출 요청

```http
POST /api/v1/checklists
Authorization: Bearer {accessToken}
Content-Type: multipart/form-data

request: {
  "templateId": 1,
  "siteName": "강남 현장",
  "workDate": "2025-01-08",
  "items": [
    {
      "templateItemId": 1,
      "answer": "YES",
      "riskFlag": false,
      "comment": null
    },
    {
      "templateItemId": 2,
      "answer": "NO",
      "riskFlag": true,
      "comment": "안전대 체결 불량"
    }
  ],
  "remarks": "오전 점검 완료"
}
files: [사진 파일들 (선택)]
```

### 내 체크리스트 목록 조회

```http
GET /api/v1/checklists/my?page=0&size=20&sort=createdAt,desc
Authorization: Bearer {accessToken}
```

---

## 5. 템플릿 API (Template)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `GET` | `/api/v1/templates` | 템플릿 목록 조회 | ✅ |
| `GET` | `/api/v1/templates?workTypeId={id}` | 작업유형별 템플릿 필터 | ✅ |
| `GET` | `/api/v1/templates/{templateId}` | 템플릿 상세 조회 | ✅ |

### 템플릿 목록 조회 응답

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "title": "고소작업 점검표",
      "workTypeName": "고소작업",
      "itemCount": 5,
      "version": 1
    },
    {
      "id": 2,
      "title": "용접작업 점검표",
      "workTypeName": "용접작업",
      "itemCount": 3,
      "version": 1
    }
  ],
  "error": null
}
```

### 템플릿 상세 조회 응답

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "고소작업 점검표",
    "description": "고소작업 전 안전점검 항목",
    "workTypeName": "고소작업",
    "version": 1,
    "items": [
      {
        "id": 1,
        "category": "개인보호구",
        "content": "안전대가 정상적으로 체결되어 있는가?",
        "itemOrder": 1,
        "isRequired": true
      },
      {
        "id": 2,
        "category": "개인보호구",
        "content": "안전모를 착용하였는가?",
        "itemOrder": 2,
        "isRequired": true
      }
    ]
  },
  "error": null
}
```

---

## 6. 위험성 평가 API (Risk)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `GET` | `/api/v1/risks/pending` | 평가 대기 위험 항목 조회 | ✅ |
| `POST` | `/api/v1/risks/{checklistItemId}/assess` | 위험성 평가 등록 | ✅ |
| `GET` | `/api/v1/risks/{assessmentId}` | 평가 상세 조회 | ✅ |
| `GET` | `/api/v1/risks/high-risk` | 고위험 항목 조회 (HIGH, CRITICAL) | ✅ |
| `GET` | `/api/v1/risks/level/{level}` | 위험 레벨별 조회 | ✅ |
| `GET` | `/api/v1/risks/countermeasures/incomplete` | 미완료 대책 목록 조회 | ✅ |
| `GET` | `/api/v1/risks/countermeasures/overdue` | 기한 초과 대책 조회 | ✅ |
| `PATCH` | `/api/v1/risks/countermeasures/{id}/complete` | 대책 완료 처리 | ✅ |

### 위험 레벨 (RiskLevel)

| 값 | 설명 | 위험도 점수 |
|----|------|------------|
| `LOW` | 저위험 | 1-4 |
| `MEDIUM` | 중위험 | 5-9 |
| `HIGH` | 고위험 | 10-15 |
| `CRITICAL` | 심각 | 16-25 |

### 위험성 평가 등록 요청

```http
POST /api/v1/risks/5/assess
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "frequency": 3,
  "severity": 4,
  "description": "안전대 체결 불량으로 추락 위험 존재"
}
```

### 응답

```json
{
  "success": true,
  "data": {
    "id": 1,
    "checklistItemId": 5,
    "frequency": 3,
    "severity": 4,
    "riskScore": 12,
    "riskLevel": "HIGH",
    "description": "안전대 체결 불량으로 추락 위험 존재",
    "assessedAt": "2025-01-08T10:30:00",
    "countermeasures": [
      {
        "id": 1,
        "content": "안전대 교체 및 체결 교육 실시",
        "status": "PLANNED",
        "dueDate": "2025-01-10"
      }
    ]
  },
  "error": null
}
```

---

## 7. 검토 API (Review)

| Method | Endpoint | 설명 | 인증 | 권한 |
|--------|----------|------|------|------|
| `POST` | `/api/v1/reviews/{checklistId}` | 체크리스트 검토 (승인/반려) | ✅ | SUPERVISOR+ |
| `GET` | `/api/v1/reviews/{checklistId}/history` | 검토 이력 조회 | ✅ | - |
| `GET` | `/api/v1/reviews/recent` | 최근 검토 이력 조회 | ✅ | - |

### 검토 액션 (ReviewAction)

| 값 | 설명 |
|----|------|
| `APPROVE` | 승인 |
| `REJECT` | 반려 |

### 체크리스트 검토 요청

```http
POST /api/v1/reviews/1
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "action": "APPROVE",
  "comment": "점검 내용 확인 완료. 승인합니다."
}
```

### 반려 요청

```http
POST /api/v1/reviews/2
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "action": "REJECT",
  "comment": "위험 항목에 대한 상세 설명이 필요합니다."
}
```

---

## 8. 파일 API (File)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `GET` | `/api/v1/files/{filename}` | 파일 다운로드 | ✅ |

### 파일 다운로드

```http
GET /api/v1/files/photo-2025-01-08-abc123.jpg
Authorization: Bearer {accessToken}
```

---

## 9. 헬스체크 API (Health)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `GET` | `/api/v1/health` | 상세 헬스체크 | ❌ |
| `GET` | `/api/v1/health/ping` | 간단한 헬스체크 | ❌ |

### 상세 헬스체크 응답

```json
{
  "success": true,
  "data": {
    "status": "UP",
    "timestamp": "2025-01-08T10:30:00",
    "application": "safety-road-inclass",
    "version": "0.0.1-SNAPSHOT",
    "environment": "dev"
  },
  "error": null
}
```

### 간단한 헬스체크 응답

```json
{
  "success": true,
  "data": {
    "status": "UP",
    "timestamp": "2025-01-08T10:30:00"
  },
  "error": null
}
```

---

## 10. 테스트용 사용자 계정

> DataSeeder에 의해 서버 시작 시 자동 생성됩니다.

| 역할 | Username | Password | Role |
|------|----------|----------|------|
| 안전관리자 | `admin` | `admin123` | ROLE_SAFETY_MANAGER |
| 관리감독자 | `supervisor1` | `supervisor123` | ROLE_SUPERVISOR |
| 작업자1 | `worker1` | `worker123` | ROLE_WORKER |
| 작업자2 | `worker2` | `worker123` | ROLE_WORKER |

---

## 빠른 테스트 (cURL)

### 헬스체크

```bash
curl http://localhost:8080/api/v1/health/ping
```

### 로그인

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"worker1","password":"worker123"}'
```

### AI 분석 (인증 불필요)

```bash
curl -X POST http://localhost:8080/api/v1/business-plan/generate \
  -H "Content-Type: application/json" \
  -d '{
    "inputType": "TEXT",
    "inputText": "고소 작업 중 안전대 미착용 상태입니다."
  }'
```

### 인증 필요한 API 호출

```bash
# 1. 먼저 로그인해서 토큰 획득
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"worker1","password":"worker123"}' | jq -r '.data.accessToken')

# 2. 토큰으로 API 호출
curl http://localhost:8080/api/v1/templates \
  -H "Authorization: Bearer $TOKEN"
```

---

## 공통 응답 형식

### 성공 응답

```json
{
  "success": true,
  "data": { ... },
  "error": null
}
```

### 에러 응답

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "에러 메시지",
    "details": { ... }
  }
}
```

### 주요 에러 코드

| 코드 | 설명 |
|------|------|
| `A001` | 인증 필요 |
| `A002` | 유효하지 않은 토큰 |
| `A003` | 토큰 만료 |
| `B001` | 잘못된 요청 |
| `B002` | 리소스 없음 |
| `B003` | 권한 없음 |
| `S001` | 서버 내부 오류 |

---

## Swagger UI 접속

모든 API를 브라우저에서 직접 테스트할 수 있습니다:

👉 **http://localhost:8080/swagger-ui/index.html**

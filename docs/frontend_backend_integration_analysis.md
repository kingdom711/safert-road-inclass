# 프론트엔드-백엔드 연동 상태 분석 보고서

> **분석 대상**
> - **Frontend**: `Life-game/safety-quest-game` (React/Vite)
> - **Backend**: `safert-road-inclass` (Spring Boot)
> - **분석 일시**: 2026-01-06

---

## 📋 연동 상태 요약

| 구분 | 프론트엔드 | 백엔드 | 연동 상태 |
|------|----------|--------|---------|
| API 모듈 수 | 13개 | 7개 Controller | 대부분 정의됨 |
| 실제 연동 | 일부 Mock 폴백 | 구현 완료 | ⚠️ 부분 연동 |

---

## 🔗 페이지별 API 연동 현황 상세표

| 페이지명 (설명) | Backend API Endpoint (용도) | 호출 조건 | 구현 여부 | Request Body 요약 | Response Body 요약 | 성공 시 동작 | 실패 시 동작 |
|----------------|---------------------------|-----------|----------|------------------|-------------------|-------------|-------------|
| **RiskSolutionPage** (AI 위험 분석 요청 페이지) | `POST /api/v1/business-plan/generate` (위험 상황 AI 분석) | "AI 솔루션 요청" 버튼 클릭 시 | **O** | `{ inputType: "TEXT"\|"PHOTO", inputText: string, photoId?: string, context?: object }` | `{ success: true, data: { riskFactor, remediationSteps[], referenceCode, riskLevel, analysisId, analyzedAt } }` | 분석 결과 화면 표시 (`step: 'result'`) | 에러 메시지 표시, 입력 화면으로 복귀 (`step: 'input'`) |
| **RiskSolutionPage** | - 서버 연결 실패 시 | 네트워크 에러 발생 시 | **Mock** | - | Mock 응답 (KOSHA 코드 기반 샘플) | Mock 결과 표시 + `fallback: true` 표시 | - |
| **Signup** (회원가입 페이지) | `POST /api/v1/auth/signup` (회원가입) | "회원가입 완료" 버튼 클릭 시 | **X** (LocalStorage만 사용) | 프론트엔드 정의: `{ email, password, name, companyName, planType }` | 백엔드 미구현 | `userProfile.setName()` 호출 후 `onSignupComplete` 콜백 | 프론트엔드 유효성 검증 에러 표시 |
| **Dashboard** (메인 대시보드) | - | 페이지 로드 시 (useEffect) | **X** (LocalStorage만) | - | - | LocalStorage에서 포인트/레벨/스트릭 로드 | - |
| **Dashboard** → "안전 지능 시스템" 버튼 | - | 클릭 시 | - | - | - | `/risk-solution` 페이지로 이동 | - |
| **LandingPage** (랜딩/소개 페이지) | - | - | **X** | - | - | 정적 페이지 (API 미사용) | - |
| **LaunchScreen** (앱 시작 화면) | - | - | **X** | - | - | 정적 페이지 (API 미사용) | - |
| **Profile** (프로필 페이지) | - | - | **X** (LocalStorage만) | - | - | LocalStorage 데이터 표시 | - |
| **Inventory** (인벤토리) | - | - | **X** (LocalStorage만) | - | - | LocalStorage 아이템 관리 | - |
| **Shop** (상점) | - | - | **X** (LocalStorage만) | - | - | LocalStorage 포인트 차감 | - |
| **DailyQuests** (일일 퀘스트) | - | - | **X** (LocalStorage만) | - | - | LocalStorage 퀘스트 상태 관리 | - |
| **WeeklyQuests** (주간 퀘스트) | - | - | **X** (LocalStorage만) | - | - | LocalStorage 퀘스트 상태 관리 | - |
| **MonthlyQuests** (월간 퀘스트) | - | - | **X** (LocalStorage만) | - | - | LocalStorage 퀘스트 상태 관리 | - |
| **PricingPage** (가격 안내) | - | - | **X** | - | - | 정적 페이지 | - |
| **TeamPage** (팀 소개) | - | - | **X** | - | - | 정적 페이지 | - |

---

## 📡 API 모듈별 엔드포인트 상세 매핑

### 1. 인증 API (`authApi.js` ↔ `AuthController.java`)

| API 함수 | HTTP Method | Endpoint | 프론트 구현 | 백엔드 구현 | 페이지 사용 여부 |
|---------|------------|----------|------------|------------|----------------|
| `signup()` | POST | `/api/v1/auth/signup` | O | **X** (미구현) | Signup.jsx ❌ 미연동 |
| `login()` | POST | `/api/v1/auth/login` | O | **O** | 미사용 |
| `logout()` | POST | `/api/v1/auth/logout` | O | **X** (미구현) | 미사용 |
| `refreshToken()` | POST | `/api/v1/auth/refresh` | O | **O** | 자동 호출 (401 시) |
| `getMe()` | GET | `/api/v1/auth/me` | O | **O** | 미사용 |
| `requestPasswordReset()` | POST | `/api/v1/auth/password-reset/request` | O | **X** (미구현) | 미사용 |
| `resetPassword()` | POST | `/api/v1/auth/password-reset/confirm` | O | **X** (미구현) | 미사용 |

### 2. GEMS AI 분석 API (`gemsApi.js` ↔ `BusinessPlanController.java`)

| API 함수 | HTTP Method | Endpoint | 프론트 구현 | 백엔드 구현 | 페이지 사용 여부 |
|---------|------------|----------|------------|------------|----------------|
| `analyzeRisk()` | POST | `/api/v1/business-plan/generate` | O | **O** | RiskSolutionPage.jsx ✅ 연동 |
| `getAnalysisHistory()` | GET | `/api/v1/business-plan/history` | O | **O** (Mock 데이터) | 미사용 |
| `checkHealth()` | GET | `/api/v1/business-plan/health` | O | **O** | 미사용 |
| `getAnalysisById()` | GET | `/api/v1/business-plan/{analysisId}` | O | **X** (미구현) | 미사용 |
| `saveActionRecord()` | POST | `/api/v1/business-plan/action-records` | O | **X** (미구현) | 미사용 |

### 3. 체크리스트 API (`checklistApi.js` ↔ `ChecklistController.java`)

| API 함수 | HTTP Method | Endpoint | 프론트 구현 | 백엔드 구현 | 페이지 사용 여부 |
|---------|------------|----------|------------|------------|----------------|
| `submitChecklist()` | POST | `/api/v1/checklists` | O | **O** | 미사용 |
| `getMyChecklists()` | GET | `/api/v1/checklists/my` | O | **O** | 미사용 |
| `getChecklistDetail()` | GET | `/api/v1/checklists/{id}` | O | **O** | 미사용 |
| `getChecklistsByStatus()` | GET | `/api/v1/checklists/status/{status}` | O | **O** | 미사용 |
| `getChecklistsWithRisk()` | GET | `/api/v1/checklists/with-risk` | O | **O** | 미사용 |

### 4. 위험성 평가 API (`riskApi.js` ↔ `RiskController.java`)

| API 함수 | HTTP Method | Endpoint | 프론트 구현 | 백엔드 구현 | 페이지 사용 여부 |
|---------|------------|----------|------------|------------|----------------|
| `getPendingRisks()` | GET | `/api/v1/risks/pending` | O | **O** | 미사용 |
| `assessRisk()` | POST | `/api/v1/risks/{itemId}/assess` | O | **O** | 미사용 |
| `getAssessmentDetail()` | GET | `/api/v1/risks/{id}` | O | **O** | 미사용 |
| `getHighRiskItems()` | GET | `/api/v1/risks/high-risk` | O | **O** | 미사용 |
| `getAssessmentsByLevel()` | GET | `/api/v1/risks/level/{level}` | O | **O** | 미사용 |
| `getIncompleteCountermeasures()` | GET | `/api/v1/risks/countermeasures/incomplete` | O | **O** | 미사용 |
| `getOverdueCountermeasures()` | GET | `/api/v1/risks/countermeasures/overdue` | O | **O** | 미사용 |
| `completeCountermeasure()` | PATCH | `/api/v1/risks/countermeasures/{id}/complete` | O | **O** | 미사용 |

### 5. 검토 API (`reviewApi.js` ↔ `ReviewController.java`)

| API 함수 | HTTP Method | Endpoint | 프론트 구현 | 백엔드 구현 | 페이지 사용 여부 |
|---------|------------|----------|------------|------------|----------------|
| `reviewChecklist()` | POST | `/api/v1/reviews/{checklistId}` | O | **O** | 미사용 |
| `getChecklistReviewHistory()` | GET | `/api/v1/reviews/{checklistId}/history` | O | **O** | 미사용 |
| `getRecentReviews()` | GET | `/api/v1/reviews/recent` | O | **O** | 미사용 |

### 6. 템플릿 API (`templateApi.js` ↔ `TemplateController.java`)

| API 함수 | HTTP Method | Endpoint | 프론트 구현 | 백엔드 구현 | 페이지 사용 여부 |
|---------|------------|----------|------------|------------|----------------|
| `getTemplates()` | GET | `/api/v1/templates` | O | **O** | 미사용 |
| `getTemplateDetail()` | GET | `/api/v1/templates/{id}` | O | **O** | 미사용 |

---

## ⚙️ 환경 설정

### 프론트엔드 환경변수 (`environment.js`)

| 변수명 | 기본값 | 설명 |
|-------|-------|------|
| `VITE_API_BASE_URL` | `http://localhost:8080` | 백엔드 서버 URL |
| `VITE_USE_MOCK` | `false` | Mock 모드 활성화 여부 |
| `VITE_API_TIMEOUT` | `30000` | API 타임아웃 (ms) |
| `VITE_DEV_MODE` | `false` | 개발 모드 (로깅 활성화) |

---

## 📊 연동 상태 요약 통계

| 카테고리 | 프론트엔드 API 정의 | 백엔드 구현 | 실제 페이지 연동 |
|---------|-----------------|-----------|----------------|
| 인증 (Auth) | 7개 | 3개 | ❌ 0개 |
| AI 분석 (GEMS) | 5개 | 3개 | ✅ 1개 (RiskSolutionPage) |
| 체크리스트 | 5개 | 5개 | ❌ 0개 |
| 위험성 평가 | 8개 | 8개 | ❌ 0개 |
| 검토 | 3개 | 3개 | ❌ 0개 |
| 템플릿 | 2개 | 2개 | ❌ 0개 |
| **합계** | **30개** | **24개** | **1개** |

---

## 🔍 핵심 발견 사항

### ✅ 정상 연동 중인 기능
1. **RiskSolutionPage ↔ BusinessPlanController**
   - `POST /api/v1/business-plan/generate` 실제 호출
   - 서버 연결 실패 시 Mock 폴백 처리 구현
   - Gemini API 연동 완료

### ⚠️ 미연동 상태 (주요)
1. **회원가입 (Signup.jsx)**: 프론트엔드에서 LocalStorage만 사용, 백엔드 signup API 미구현
2. **로그인/로그아웃**: API 모듈 정의만 되어 있고 실제 사용 안 함
3. **체크리스트/위험성 평가/검토**: 백엔드 완전 구현, 프론트엔드 UI 미연동
4. **Dashboard**: 모든 데이터 LocalStorage 기반

### 🔧 Mock 폴백 동작
- `gemsApi.js`에서 네트워크 에러 시 자동으로 Mock 응답 반환
- Mock 응답에는 `isMock: true` 또는 `fallback: true` 플래그 포함
- KOSHA 코드 기반 샘플 데이터 5종 내장

---

## 📁 관련 파일 경로

### 프론트엔드 (Life-game/safety-quest-game)
- API 모듈: `src/api/*.js`
- 페이지: `src/pages/*.jsx`
- 환경설정: `src/config/environment.js`

### 백엔드 (safert-road-inclass)
- 컨트롤러: `src/main/java/com/jinsung/safety_road_inclass/domain/*/controller/*Controller.java`
- 설정: `src/main/resources/application.properties`

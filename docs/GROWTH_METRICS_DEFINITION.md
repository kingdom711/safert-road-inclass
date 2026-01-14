# 📊 핵심 지표 정의서 (Growth Metrics Definition)

> **작성일**: 2024-12-17  
> **버전**: 1.0.0  
> **목적**: 그로스 해킹 전략을 실제 제품에 적용하기 위한 구체적인 지표 및 로깅 명세

---

## Table 1. 북극성 지표(NSM) 구조

| 구분 | 지표명 | 정의(Definition) | 측정 주기 | 목표치(초기) | 데이터 소스 |
|---|---|---|---|---|---|
| **NSM** | 월간 활성 조직 수 (MAO) | 한 달 동안 최소 1건 이상의 체크리스트를 제출하고, 그 중 최소 1건이 승인 완료 상태에 도달한 조직(회사/현장)의 총 개수 | 월간 | 50개 (3개월) | 백엔드 DB: `checklists` 테이블 + `review_logs` 테이블 조인 |
| **Input 1** | 주간 신규 조직 온보딩 수 | 매주 새로 가입하고 최소 1명의 사용자 계정을 생성한 조직 수. 조직 가입일 기준으로 주 단위 집계 | 주간 | 주 5개 (4주차) | 백엔드 DB: `organizations` 테이블 (created_at 기준) + `users` 테이블 조인 |
| **Input 2** | 조직당 주간 체크리스트 제출 건수 | 활성 조직당 주간 평균 체크리스트 제출 건수. 계산식: (주간 총 제출 건수) ÷ (해당 주에 최소 1건 이상 제출한 조직 수) | 주간 | 조직당 주 2건 (초기) | 백엔드 DB: `checklists` 테이블 (status='SUBMITTED', created_at 기준) |
| **Input 3** | 조직당 주간 활성 역할 수 | 한 조직 내에서 주간에 실제 활동한 역할(기술인/관리감독자/안전관리자)의 평균 개수. 활동 기준: 로그인 + 최소 1건 이상의 액션(작성/승인/조회 등) | 주간 | 조직당 1.5개 역할 (초기) | 백엔드 DB: `users` 테이블 (role) + `audit_logs` 또는 활동 이벤트 로그 조인 |

---

## Table 2. AARRR 단계별 핵심 행동 (Key Actions)

| 단계 | 핵심 행동 (User Action) | 측정 기준 (Metric) | 성공 기준 | 비고 |
|---|---|---|---|---|
| **Acquisition** | 랜딩페이지 방문 후 "데모 신청" 또는 "무료 체험 시작" 버튼 클릭 | 랜딩페이지 방문 대비 CTA 클릭률 | 3% 이상 | GA4 이벤트: `cta_click` (category: 'Acquisition', action: 'demo_request' 또는 'trial_start') |
| **Acquisition** | 데모 신청 폼 제출 완료 (이름, 회사명, 연락처 입력 후 제출) | CTA 클릭 대비 폼 제출 완료율 | 50% 이상 | GA4 이벤트: `form_submit` (category: 'Acquisition', action: 'demo_form_complete') |
| **Acquisition** | 조직 가입 완료 (회사명, 현장명 등록 및 첫 사용자 계정 생성) | 폼 제출 대비 실제 가입 완료율 | 30% 이상 | 백엔드 로그: `POST /api/organizations` 성공 + `POST /api/auth/signup` 성공 |
| **Activation** | 가입 후 24시간 이내 첫 로그인 완료 | 가입 완료 대비 24시간 내 로그인율 | 60% 이상 | 백엔드 로그: `POST /api/auth/login` 성공 (가입일 기준 24시간 이내) |
| **Activation** | 첫 로그인 후 체크리스트 작성 화면 진입 (작업유형 선택 또는 템플릿 로딩) | 첫 로그인 대비 체크리스트 작성 시작율 | 70% 이상 | 프론트엔드 GA4: `page_view` ('/checklist/create') 또는 백엔드 로그: `GET /api/templates?workType=*` |
| **Activation** | 첫 체크리스트 제출 완료 (작업유형 선택 → 항목 응답 → 사진 업로드(선택) → 제출 버튼 클릭) | 체크리스트 작성 시작 대비 제출 완료율 | 80% 이상 | 백엔드 로그: `POST /api/checklists` 성공 (status='SUBMITTED') |
| **Activation** | 첫 체크리스트 제출 후 48시간 이내 감독자 승인 완료 | 제출 완료 대비 48시간 내 승인율 | 50% 이상 | 백엔드 로그: `POST /api/reviews` 성공 (action='APPROVE', 제출일 기준 48시간 이내) |
| **Retention** | 주간 재방문 (주간에 최소 1회 로그인) | 활성 조직 중 주간 재방문 조직 비율 | 60% 이상 | 백엔드 로그: `POST /api/auth/login` 성공 (이전 로그인일 기준 7일 이내) |
| **Retention** | 주간 체크리스트 제출 (주간에 최소 1건 이상 제출) | 활성 조직 중 주간 제출 조직 비율 | 40% 이상 | 백엔드 DB: `checklists` 테이블 (주간 집계) |
| **Retention** | 역할별 "내 할 일" 대시보드 조회 (기술인: 승인 대기 목록, 감독자: 검토 필요 목록, 안전관리자: 위험현황 목록) | 주간 로그인 대비 역할별 대시보드 조회율 | 기술인 70%, 감독자 80%, 안전관리자 60% | 프론트엔드 GA4: `page_view` ('/dashboard') 또는 백엔드 로그: `GET /api/checklists?status=*` |
| **Retention** | 게이미피케이션 퀘스트 완료 (일일 퀘스트: 체크리스트 1건 제출, 주간 퀘스트: 5일 연속 제출 등) | 활성 사용자 중 퀘스트 완료율 | 30% 이상 | 프론트엔드 GA4: `quest_complete` (category: 'Engagement', action: 'quest_complete') |
| **Revenue** | 무료 체험 기간 종료 전 유료 플랜 선택 (14일 체험 또는 월 20건 사용 후 플랜 선택 화면 진입) | 체험 조직 중 플랜 선택 진입율 | 40% 이상 | 프론트엔드 GA4: `page_view` ('/pricing') 또는 `plan_selected` 이벤트 |
| **Revenue** | 결제 시작 (Checkout 화면 진입 또는 결제 버튼 클릭) | 플랜 선택 대비 결제 시작율 | 60% 이상 | 프론트엔드 GA4: `begin_checkout` (category: 'Conversion', action: 'begin_checkout') |
| **Revenue** | 결제 완료 (유료 플랜 구독 성공) | 결제 시작 대비 결제 완료율 | 80% 이상 | 프론트엔드 GA4: `purchase` (category: 'Conversion', action: 'purchase') + 백엔드 로그: 결제 API 성공 |
| **Revenue** | 월간 구독 갱신 (구독 만료 7일 전 갱신 또는 자동 갱신) | 구독 조직 중 갱신 완료율 | 90% 이상 | 백엔드 DB: `subscriptions` 테이블 (renewal_date 기준) |
| **Referral** | 조직 내 역할 확장 (기술인 가입 후 감독자 또는 안전관리자 추가 가입) | 기술인만 있는 조직 중 역할 확장 조직 비율 | 30% 이상 | 백엔드 DB: `users` 테이블 (조직별 role 종류 수 집계) |
| **Referral** | 추천 프로그램 사용 (기존 고객이 추천 링크를 통해 새 조직 가입) | 신규 가입 중 추천을 통한 가입 비율 | 30% 이상 | 백엔드 DB: `organizations` 테이블 (referral_code 필드 존재 여부) |
| **Referral** | 성공 사례 공유 (케이스 스터디 다운로드 또는 리포트 공유) | 활성 조직 중 공유 이벤트 발생율 | 10% 이상 | 프론트엔드 GA4: `share` (category: 'Referral', action: 'case_study_download' 또는 'report_share') |

---

## Table 3. 데이터 로깅이 필요한 후보 리스트 (Draft)

### 3.1 이미 측정되고 있는 로그 (Existing Logs)

| 화면명/API | 사용자 행동 | 예상되는 데이터 | 로깅 위치 | 상태 |
|---|---|---|---|---|
| **랜딩페이지** (`/landing`) | 페이지뷰 | `pagePath: '/landing'`, `pageTitle: 'Landing Page - 안전의 길'` | 프론트엔드 GA4: `analytics.pageView()` | ✅ 구현됨 |
| **가입 페이지** (`/signup`) | 페이지뷰 | `pagePath: '/signup'`, `pageTitle: 'Signup - 회원가입'` | 프론트엔드 GA4: `analytics.pageView()` | ✅ 구현됨 |
| **가입 완료** | 회원가입 완료 | `category: 'Conversion'`, `action: 'signup_complete'`, `label: planType` | 프론트엔드 GA4: `analytics.conversion.signupComplete()` | ✅ 구현됨 |
| **요금제 페이지** (`/pricing`) | 페이지뷰 | `pagePath: '/pricing'`, `pageTitle: 'Pricing Page - 요금제'` | 프론트엔드 GA4: `analytics.pageView()` | ✅ 구현됨 |
| **플랜 선택** | 플랜 선택 버튼 클릭 | `category: 'Conversion'`, `action: 'plan_selected'`, `label: planType`, `value: price` | 프론트엔드 GA4: `analytics.conversion.planSelected()` | ✅ 구현됨 |
| **결제 시작** | 결제 버튼 클릭 | `category: 'Conversion'`, `action: 'begin_checkout'`, `label: planType`, `value: amount` | 프론트엔드 GA4: `analytics.conversion.checkoutStart()` | ✅ 구현됨 |
| **결제 완료** | 결제 성공 | `category: 'Conversion'`, `action: 'purchase'`, `transaction_id`, `value: amount` | 프론트엔드 GA4: `analytics.conversion.purchase()` | ✅ 구현됨 |
| **출석 체크** | 일일 출석 체크 | `category: 'Engagement'`, `action: 'daily_check_in'`, `label: streak_${days}`, `value: bonusPoints` | 프론트엔드 GA4: `analytics.engagement.dailyCheckIn()` | ✅ 구현됨 |
| **로그인 API** (`POST /api/auth/login`) | 로그인 성공 | `userId`, `username`, `role`, `timestamp` | 백엔드 로그: `log.info("로그인 성공: userId={}, username={}, role={}")` | ✅ 구현됨 |
| **체크리스트 제출 API** (`POST /api/checklists`) | 체크리스트 제출 완료 | `checklistId`, `userId`, `riskCount`, `timestamp` | 백엔드 로그: `log.info("체크리스트 제출 완료: checklistId={}, userId={}, riskCount={}")` | ✅ 구현됨 |
| **체크리스트 승인/반려 API** (`POST /api/reviews`) | 검토 완료 (승인/반려) | `checklistId`, `reviewerId`, `action`, `comment`, `timestamp` | 백엔드 로그: `log.info("체크리스트 승인/반려: checklistId={}, reviewerId={}")` + `ReviewLog` 엔티티 저장 | ✅ 구현됨 |
| **AI 분석 API** (`POST /api/ai/analyze`) | AI 분석 요청/완료 | `requestId`, `inputType`, `riskLevel`, `duration`, `timestamp` | 백엔드 로그: `log.info("[GEMS AI 요청 시작/완료] requestId={}, riskLevel={}")` | ✅ 구현됨 |
| **API 호출 메트릭** (모든 API) | API 요청/응답 | `requestId`, `method`, `path`, `status`, `durationMs` | 백엔드 로그: `LoggingFilter`에서 `log.info("METRIC:API_CALL ...")` | ✅ 구현됨 |

### 3.2 추가로 로그 기록이 필요한 부분 (Required Logs)

| 화면명/API | 사용자 행동 | 예상되는 데이터 | 로깅 위치 | 우선순위 |
|---|---|---|---|---|
| **랜딩페이지** (`/landing`) | "데모 신청" 또는 "무료 체험 시작" 버튼 클릭 | `category: 'Acquisition'`, `action: 'cta_click'`, `label: 'demo_request'` 또는 `'trial_start'` | 프론트엔드 GA4: `analytics.event()` 추가 | 🔴 High |
| **데모 신청 폼** | 폼 제출 완료 (이름, 회사명, 연락처 입력 후 제출) | `category: 'Acquisition'`, `action: 'demo_form_complete'`, `label: companyName` | 프론트엔드 GA4: `analytics.event()` 추가 | 🔴 High |
| **조직 가입 API** (`POST /api/organizations`) | 조직 가입 완료 | `organizationId`, `companyName`, `siteName`, `createdBy`, `timestamp` | 백엔드 로그: `log.info("조직 가입 완료: organizationId={}, companyName={}")` 추가 | 🔴 High |
| **첫 로그인 후 대시보드** (`/dashboard`) | 대시보드 첫 진입 | `category: 'Activation'`, `action: 'dashboard_first_view'`, `label: role` | 프론트엔드 GA4: `analytics.event()` 추가 | 🔴 High |
| **체크리스트 작성 화면** (`/checklist/create`) | 체크리스트 작성 시작 (작업유형 선택 또는 템플릿 로딩) | `category: 'Activation'`, `action: 'checklist_create_start'`, `label: workType` | 프론트엔드 GA4: `analytics.event()` 추가 또는 백엔드 로그: `GET /api/templates?workType=*` | 🔴 High |
| **체크리스트 임시저장** (`POST /api/checklists/draft`) | 임시저장 완료 | `checklistId`, `userId`, `status: 'DRAFT'`, `timestamp` | 백엔드 로그: `log.info("체크리스트 임시저장: checklistId={}, userId={}")` 추가 | 🟡 Medium |
| **템플릿 로딩 API** (`GET /api/templates?workType=*`) | 템플릿 로딩 완료 (성능 측정) | `templateId`, `workType`, `itemCount`, `loadTimeMs`, `timestamp` | 백엔드 로그: `log.info("템플릿 로딩 완료: templateId={}, loadTimeMs={}")` 추가 | 🟡 Medium |
| **"내 할 일" 대시보드** (`/dashboard/todos`) | 역할별 할 일 목록 조회 | `category: 'Retention'`, `action: 'todo_list_view'`, `label: role`, `value: itemCount` | 프론트엔드 GA4: `analytics.event()` 추가 | 🔴 High |
| **체크리스트 승인 대기 목록** (`GET /api/checklists?status=SUBMITTED`) | 기술인이 승인 대기 목록 조회 | `userId`, `role: 'TECHNICIAN'`, `status: 'SUBMITTED'`, `count`, `timestamp` | 백엔드 로그: `log.info("승인 대기 목록 조회: userId={}, count={}")` 추가 | 🟡 Medium |
| **검토 필요 목록** (`GET /api/reviews/pending`) | 감독자가 검토 필요 목록 조회 | `userId`, `role: 'SUPERVISOR'`, `count`, `timestamp` | 백엔드 로그: `log.info("검토 필요 목록 조회: userId={}, count={}")` 추가 | 🟡 Medium |
| **위험현황 목록** (`GET /api/risks?priority=HIGH`) | 안전관리자가 위험현황 조회 | `userId`, `role: 'SAFETY_MANAGER'`, `priority`, `count`, `timestamp` | 백엔드 로그: `log.info("위험현황 조회: userId={}, priority={}, count={}")` 추가 | 🟡 Medium |
| **퀘스트 완료** | 게이미피케이션 퀘스트 완료 (일일/주간/월간) | `category: 'Engagement'`, `action: 'quest_complete'`, `label: questType_questId`, `value: pointsEarned` | 프론트엔드 GA4: `analytics.engagement.questComplete()` 추가 (현재 구조에 있으나 실제 호출 필요) | 🟡 Medium |
| **재방문 (로그인)** | 주간 재방문 (이전 로그인일 기준 7일 이내) | `userId`, `organizationId`, `daysSinceLastLogin`, `timestamp` | 백엔드 로그: `log.info("재방문: userId={}, daysSinceLastLogin={}")` 추가 (로그인 시 계산) | 🔴 High |
| **조직 내 역할 확장** | 조직 내 두 번째 이상 역할 가입 (기술인 → 감독자 등) | `organizationId`, `newRole`, `existingRoles`, `totalRoles`, `timestamp` | 백엔드 로그: `log.info("역할 확장: organizationId={}, newRole={}, totalRoles={}")` 추가 (가입 시 계산) | 🟡 Medium |
| **추천 프로그램** | 추천 링크를 통한 가입 | `organizationId`, `referrerOrganizationId`, `referralCode`, `timestamp` | 백엔드 DB: `organizations` 테이블에 `referral_code`, `referrer_organization_id` 필드 추가 및 로깅 | 🟡 Medium |
| **플랜 업그레이드** | 기본 플랜 → 프리미엄 플랜 업그레이드 | `userId`, `organizationId`, `previousPlan`, `newPlan`, `amount`, `timestamp` | 프론트엔드 GA4: `analytics.conversion.planUpgrade()` 추가 + 백엔드 로그 | 🟡 Medium |
| **구독 갱신** | 월간 구독 갱신 (자동 또는 수동) | `subscriptionId`, `organizationId`, `renewalDate`, `amount`, `timestamp` | 백엔드 로그: `log.info("구독 갱신: subscriptionId={}, organizationId={}")` 추가 | 🔴 High |
| **이탈 위험 신호** | 30일 미사용 조직 (마지막 활동일 기준) | `organizationId`, `lastActivityDate`, `daysInactive`, `timestamp` | 백엔드 스케줄러: 주간 집계 로그 또는 별도 테이블 (`inactive_organizations`) | 🟡 Medium |
| **성공 사례 다운로드** | 케이스 스터디 PDF 다운로드 | `category: 'Referral'`, `action: 'case_study_download'`, `label: caseStudyId` | 프론트엔드 GA4: `analytics.event()` 추가 | 🟢 Low |
| **리포트 공유** | 안전관리 리포트 공유 (이메일/링크) | `category: 'Referral'`, `action: 'report_share'`, `label: shareMethod` | 프론트엔드 GA4: `analytics.event()` 추가 | 🟢 Low |

---

## 4. 로깅 구현 우선순위

### 4.1 즉시 구현 필요 (1주일 내)

1. **랜딩페이지 CTA 클릭 추적** (Acquisition 핵심)
2. **조직 가입 완료 로깅** (NSM Input 1 측정)
3. **첫 체크리스트 작성 시작 추적** (Activation 핵심)
4. **재방문 로깅** (Retention 핵심)
5. **"내 할 일" 대시보드 조회 추적** (Retention 핵심)

### 4.2 단기 구현 (1개월 내)

1. **템플릿 로딩 성능 로깅** (Activation 최적화)
2. **역할별 대시보드 조회 로깅** (Retention 분석)
3. **퀘스트 완료 추적** (Retention 게이미피케이션)
4. **구독 갱신 로깅** (Revenue 핵심)
5. **조직 내 역할 확장 추적** (Referral 내부 바이럴)

### 4.3 중기 구현 (3개월 내)

1. **추천 프로그램 추적** (Referral 외부 바이럴)
2. **플랜 업그레이드 추적** (Revenue 확장)
3. **이탈 위험 신호 모니터링** (Retention 개선)
4. **성공 사례/리포트 공유 추적** (Referral 간접)

---

## 5. 데이터 수집 및 분석 도구

### 5.1 프론트엔드 (GA4)

- **이벤트 추적**: `analytics.js` 유틸리티 확장
- **커스텀 이벤트**: AARRR 단계별 이벤트 추가
- **대시보드**: GA4 커스텀 리포트 생성

### 5.2 백엔드 (구조화된 로깅)

- **SLF4J/Logback**: 기존 로깅 인프라 활용
- **AuditLog 엔티티**: 주요 비즈니스 이벤트 DB 저장 (향후 구현)
- **메트릭 수집**: LoggingFilter 확장 또는 별도 메트릭 수집기 도입

### 5.3 데이터 분석

- **주간 리포트**: NSM 및 Input Metrics 자동 집계
- **대시보드**: 실시간 지표 모니터링 (Grafana 등)
- **알림**: 목표치 미달 시 자동 알림

---

**문서 작성자**: 그로스 해킹 전문 데이터 분석가  
**최종 업데이트**: 2024-12-17

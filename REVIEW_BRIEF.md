# 안전의 길 — 리뷰 요청 브리핑

> 리뷰어께서 5분 안에 프로젝트를 파악하고, 항목별로 바로 해당 코드로 이동하실 수 있도록 정리했습니다.
> 각 요청 항목은 **현재 상태 → 관련 코드 → 질문** 순서입니다.

## 1. 프로젝트 한 줄 소개

건설 현장의 법정 안전교육·위험성평가·안전활동을 온라인으로 진행하고, 게임화(포인트·골드·퀘스트·랭킹)로 참여를 유도하는 서비스입니다. 현장 근로자용 모바일 웹앱(PWA)과 관리자 기능으로 구성됩니다.

## 2. 저장소 구성

| 저장소 | 역할 | 링크 |
|---|---|---|
| `safert-road-inclass` | 백엔드 (Spring Boot API) | https://github.com/kingdom711/safert-road-inclass |
| `Life-game` (안의 `safety-quest-game/`) | 프론트엔드 (React SPA/PWA) | https://github.com/kingdom711/Life-game |

## 3. 기술 스택 · 배포 · 규모

**백엔드** — Java 21, Spring Boot 3.3.6, Spring Data JPA, Spring Security + JWT, springdoc(Swagger).
DB는 개발 SQLite(`ddl-auto=update`) / 운영 PostgreSQL(Supabase) 이원화. AI 분석 Gemini, PDF 생성 PDFBox. **Cloudtype** 배포.
main 소스 347개 파일 / 약 24,000 LOC, 컨트롤러 36개, 도메인 25개, 테스트 20개 파일.

**프론트엔드** — React 18 + Vite 5, Tailwind CSS 4, react-router. PWA(vite-plugin-pwa + Workbox) 오프라인 지원, IndexedDB(idb), GA4. **Vercel** 배포. 약 49,000 LOC.

**개발 배경** — 개발자 1인이 AI 코딩 도구(Claude Code, Codex)를 적극 활용해 개발했습니다. 도메인별 생성 시점이 달라 코드 스타일·구조의 일관성이 고르지 않을 수 있음을 인지하고 있으며, 그 부분의 우선순위 지적을 특히 원합니다.

## 4. 실행 방법

**백엔드** (JDK 21, 기본 포트 8080, 기본 프로파일 `dev` → SQLite라 별도 DB 설치 불필요):

```bash
cd safert-road-inclass
./gradlew bootRun        # Windows: gradlew.bat bootRun
```

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- AI 기능은 `GEMINI_API_KEY` 환경변수 필요 (없어도 서버는 뜹니다)

**프론트엔드**:

```bash
cd Life-game/safety-quest-game
npm install
cp .env.example .env     # 값은 파일 내 주석 참고
npm run dev              # http://localhost:5173
```

**추천 코드 훑기 경로 (30분)** — ① 이 문서 → ② `global/config/SecurityConfig.java` → ③ `domain/auth` (JWT 발급·검증) → ④ `domain/education`·`compliance`·`attendance` 서비스 → ⑤ 프론트 `src/api/apiClient.js` → `src/context`

---

## 5. 리뷰 요청 A — 코드 리뷰 (우선순위순)

### A-1. 시크릿·키 관리 검증 🔴

- **현재 상태**: 공개 저장소에 커밋됐던 dev용 JWT 시크릿이 운영에서도 쓰이던 문제를 발견하여, `JWT_SECRET` 환경변수 주입으로 전환 완료(prod는 기본값 없이 강제 → 미설정 시 기동 차단). Cloudtype 환경변수 설정 예정.
- **관련 코드**: `src/main/resources/application.properties` (JWT Configuration 섹션), `application-prod.properties`, `domain/auth/service/JwtTokenProvider.java`
- **질문**: 이 전환 방식이 적절한지, 그 외 키·자격증명 관리에서 놓친 부분이 있는지 봐주세요.

### A-2. 법정교육 증빙 무결성 🔴

- **현재 상태**: 교육 이수·출석 기록을 법정 증빙으로 쓰는 것이 목표. 본인확인은 목업 단계(A-B2 참고).
- **관련 코드**: `domain/education`, `domain/compliance`, `domain/attendance` 의 service 계층
- **질문**: 교육시간 인정·출석 로직이 클라이언트 조작에 안전한지, 법정 증빙 수준의 서버측 검증·감사기록이 되어 있는지 봐주세요.

### A-3. DB 이원화·마이그레이션 리스크 🟡

- **현재 상태**: dev SQLite + `ddl-auto=update`, prod PostgreSQL. **prod도 현재 Flyway 비활성 + `ddl-auto=update`** 상태(마이그레이션 파일 세트 미준비).
- **관련 코드**: `application-dev.properties`, `application-prod.properties`, `docs/historical-migrations/`
- **질문**: 스키마 드리프트 위험과, Flyway+validate 체제로 안전하게 전환하는 순서를 조언해주세요.

### A-4. 도메인 구조 일관성·리팩토링 우선순위 🟡

- **현재 상태**: 25개 도메인 / 36개 컨트롤러 (auth, attendance, education, compliance, hazardcycle, risk, checklist, team, ranking, quest, reward, point, gold, inventory, exchange, workstop, report, admin, ai 등)
- **관련 코드**: `src/main/java/com/jinsung/safety_road_inclass/domain/*`
- **질문**: 중복되거나 경계가 잘못 잡힌 도메인이 있다면 어디부터 손대는 게 효과적일지 우선순위를 짚어주세요.

### A-5. 테스트 전략 🟡

- **현재 상태**: main 347개 파일 대비 테스트 20개 파일로 매우 적음.
- **관련 코드**: `src/test/java`
- **질문**: 포인트·골드·보상 등 정합성이 민감한 로직 위주로, 어떤 계층(단위/통합)부터 늘리는 게 좋을지 봐주세요.

### A-6. (선택) 프론트 상태관리·오프라인 구조 🟢

- **현재 상태**: Context 기반 전역 상태, PWA 오프라인 저장(IndexedDB) + 서버 동기화.
- **관련 코드**: 프론트 `src/context`, `src/workers`(서비스워커), `src/api/apiClient.js`
- **질문**: 이 규모(49k LOC)에 Context 상태관리가 적절한지, 오프라인 동기화 구조의 신뢰성을 봐주세요.

## 6. 리뷰 요청 B — 방향성 자문 (현재 고민 중인 3가지)

### B-1. "앱처럼 설치" 요구 — PWA 설치 UX

- **배경**: 회사에서 웹 주소가 아니라 휴대폰에 앱처럼 설치해 쓰게 해달라는 요청.
- **현재 상태**: PWA 세팅 완료(manifest, 192/512/maskable 아이콘, `display: standalone`, 서비스워커, apple-touch-icon) → 홈 화면 설치 자체는 가능. 다만 **설치 안내 UI가 없음** — `beforeinstallprompt` 핸들링 미구현, iOS는 자동 배너 미지원이라 사용자가 직접 "공유 → 홈 화면에 추가" 필요.
- **관련 코드**: 프론트 `vite.config.js`(VitePWA 설정), `index.html`
- **질문**: ① 현장 근로자 대상 iOS/안드로이드별 설치 안내 UX를 어떻게 붙이는 게 좋을지 ② 장기적으로 PWA로 충분한지, Play스토어 등록(TWA)까지 가야 할지.

### B-2. 본인인증 — 포트원 도입 검토

- **현재 상태**: 휴대폰 인증 흐름이 **전부 목업** — 서버가 인증코드를 생성해 응답에 그대로 반환, `ConcurrentHashMap` 메모리 저장(재시작 시 소실, 만료·재시도 제한 없음).
- **관련 코드**: `domain/auth/controller/VerificationController.java`, 프론트 `src/components/IdentityVerificationModal.jsx`
- **질문**: ① 법정교육 증빙 목적이면 단순 SMS 인증으로 충분한지, 통신사 본인확인(CI/DI)까지 필요한지 ② CI/DI 등 민감정보의 개인정보보호법상 보관·암호화 의무를 어떻게 맞춰야 하는지 ③ 인증 빈도(계정당 1회 vs 출석마다 — 건당 과금이라 비용 직결)를 어떻게 설계할지.

### B-3. 보상의 유인효과가 약함

- **현재 상태**: 포인트·골드·퀘스트·랭킹·부서 배틀·실물 교환까지 구현했으나 사용 유인이 크지 않음. 보상 설계 문제인지, 설치·인증 단계의 진입 마찰 때문인지 구분이 안 된 상태. GA4는 연동돼 있음.
- **관련 코드**: `domain/point`·`gold`·`reward`·`quest`·`ranking`·`exchange`, 프론트 `src/api/departmentBattleApi.js`
- **질문**: ① 이탈 지점을 GA4 등 데이터로 진단하는 방법 ② 현장 근로자층에 포인트 외 어떤 지렛대(법정 이수 인정, 경쟁·소셜, 실물 보상)를 우선 강화할지.

## 7. 리뷰 요청 C — 시간이 남으면

- **오프라인 현장 대응**: 건설현장은 통신이 불안정한데 출석·인증은 온라인 필요. 오프라인 상황 처리 방향.
- **배포 구성 적정성**: 백엔드 Cloudtype + 프론트 Vercel 구성이 실제 도입 후 동시 사용자 증가를 버틸지, 비용은 적정한지.
- **멀티테넌시**: 여러 현장·회사를 한 서비스로 운영할 때의 데이터 격리·권한 구조.

## 8. 알고 있는 정리 예정 항목 (지적 불필요)

- 프론트 `src/Frontend_codebase/apiLogger.js` 위치 부적절(실사용 코드라 이동 예정)
- dev 프로파일 로컬 기동 시 SQLite Hibernate 메타데이터 조회 에러(`too many terms in compound SELECT`) — 인지하고 있는 기존 이슈

## 9. 시연

- 운영 백엔드: https://port-0-safert-road-inclass-mjr54f7m2ffa493e.sel3.cloudtype.app (Swagger: `/swagger-ui/index.html`)
- 운영 프론트: (Vercel 배포 URL — 공유 시 기입)

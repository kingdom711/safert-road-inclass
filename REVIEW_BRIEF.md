# 안전의 길 — 리뷰 요청 브리핑

> 코드 리뷰를 요청드리며 프로젝트를 5분 안에 파악하실 수 있도록 정리한 문서입니다.

## 1. 프로젝트 한 줄 소개

건설 현장의 법정 안전교육·위험성평가·안전활동을 온라인으로 진행하고, 게임화(포인트·골드·퀘스트·랭킹)로 참여를 유도하는 서비스입니다. 현장 근로자용 모바일 웹앱(PWA)과 관리자 기능으로 구성됩니다.

## 2. 저장소 구성

| 저장소 | 역할 | 링크 |
|---|---|---|
| `safert-road-inclass` | 백엔드 (Spring Boot API) | https://github.com/kingdom711/safert-road-inclass |
| `Life-game` (안의 `safety-quest-game/`) | 프론트엔드 (React SPA/PWA) | https://github.com/kingdom711/Life-game |

## 3. 기술 스택 · 배포

**백엔드** — Java 21, Spring Boot 3.3.6, Spring Data JPA, Spring Security + JWT, springdoc(Swagger).
DB는 개발 SQLite(`ddl-auto=update`) / 운영 PostgreSQL(Supabase, Flyway 마이그레이션) 이원화.
AI 분석에 Gemini API, 위험성평가표 PDF 생성에 PDFBox 사용. **Cloudtype**에 배포 중.

**프론트엔드** — React 18 + Vite 5, Tailwind CSS 4, react-router. PWA(vite-plugin-pwa + Workbox)로
오프라인 지원, IndexedDB(idb) 로컬 저장, GA4 연동. **Vercel**에 배포 중.

## 4. 규모

- 백엔드: main 소스 347개 파일 / 약 24,000 LOC, 컨트롤러 36개, 도메인 25개
  (auth, attendance, education, compliance, hazardcycle, risk, checklist, team, ranking, quest, reward, point, gold, inventory, exchange, workstop, report, admin, ai 등)
- 백엔드 테스트: 20개 파일 (커버리지 낮음 — 리뷰 요청 사항 5번 참고)
- 프론트엔드: 약 49,000 LOC (pages / components / api / context / hooks / workers 구조)

## 5. 실행 방법

**백엔드** (JDK 21 필요, 기본 포트 8080, 기본 프로파일 `dev` → SQLite라 별도 DB 설치 불필요):

```bash
cd safert-road-inclass
./gradlew bootRun        # Windows: gradlew.bat bootRun
```

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- AI 기능을 쓰려면 `GEMINI_API_KEY` 환경변수 필요 (없어도 서버는 뜹니다)

**프론트엔드**:

```bash
cd Life-game/safety-quest-game
npm install
cp .env.example .env     # 값은 파일 내 주석 참고
npm run dev              # http://localhost:5173
```

## 6. 개발 배경과 코드 안내

- 개발자 1인이 AI 코딩 도구(Claude Code, Codex)를 적극 활용해 개발했습니다. 도메인별로 생성 시점이 달라 **코드 스타일·구조의 일관성이 고르지 않을 수 있음**을 인지하고 있고, 그 부분의 우선순위 지적을 특히 원합니다.
- 백엔드 핵심 로직: `src/main/java/com/jinsung/safety_road_inclass/domain/*/service`
- 인증·보안 설정: `global/config/SecurityConfig.java`, JWT 관련은 `domain/auth`
- 프론트 API 레이어: `src/api/*.js` (도메인별 분리), 전역 상태는 `src/context`
- 알고 있는 정리 예정 항목: 프론트 `src/Frontend_codebase/apiLogger.js`의 위치가 부적절함(실사용 코드), 백엔드 dev/prod 스키마 관리 방식 이원화

## 7. 중점 리뷰 요청 사항

1. **시크릿 관리** — `application.properties`의 기본 `jwt.secret`이 운영 프로파일에서 오버라이드되지 않아 운영에서도 그대로 쓰이는 것으로 보입니다. 환경변수 주입 구조로 바꾸는 방법과, 그 외 키 관리 전반에서 놓친 부분을 봐주세요.
2. **법정교육 증빙 무결성** — `education`/`compliance`/`attendance` 도메인의 본인확인·교육시간 인정 로직이 클라이언트 조작에 안전한지, 법정 증빙 자료로 쓸 수준의 서버측 검증이 되어 있는지 봐주세요.
3. **DB 이원화 리스크** — 개발 SQLite + `ddl-auto=update`, 운영 PostgreSQL + Flyway 구조인데, 스키마 드리프트 위험과 안전한 운영 배포 전략 관점에서 조언 부탁드립니다.
4. **구조 일관성·리팩토링 우선순위** — 25개 도메인/36개 컨트롤러 중 중복되거나 경계가 잘못 잡힌 부분이 있다면, 어디부터 손대는 게 효과적일지 우선순위를 짚어주세요.
5. **테스트 전략** — 테스트가 20개 파일로 매우 적습니다. 포인트·골드·보상 등 정합성이 민감한 로직 위주로, 어떤 계층(단위/통합)부터 늘리는 게 좋을지 봐주세요.
6. **(선택) 프론트 상태·오프라인 구조** — PWA 오프라인 저장(IndexedDB)과 서버 동기화 구조의 신뢰성, Context 기반 상태관리가 이 규모(49k LOC)에 적절한지 의견 주시면 감사하겠습니다.

## 8. 시연

- 운영 백엔드: https://port-0-safert-road-inclass-mjr54f7m2ffa493e.sel3.cloudtype.app (Swagger: `/swagger-ui/index.html`)
- 운영 프론트: (Vercel 배포 URL — 공유 시 기입)

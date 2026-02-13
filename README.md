# Safety Road Backend (`safert-road-inclass`)

안전의 길 서비스의 Spring Boot 백엔드입니다.
인증/JWT, 체크리스트, 위험도 평가, 포인트/골드/보상, 인벤토리, 출석, 알림, Gemini 기반 AI 분석 API를 제공합니다.

## 기술 스택

- Language: Java 21
- Framework: Spring Boot 3.3.6
- Build: Gradle 8 (Wrapper 포함)
- ORM: Spring Data JPA
- Security: Spring Security + JWT
- API Docs: springdoc-openapi (Swagger UI)
- AI: Spring AI Vertex Gemini + Legacy Gemini 호출 호환
- DB
- 개발(`dev`): SQLite (`./data/safetyroad-dev.db`)
- 운영(`prod`): PostgreSQL (Supabase)

## 실행 환경

### 요구사항

- JDK 21
- Gradle Wrapper 사용 권장(`gradlew`/`gradlew.bat`)

### 실행

```bash
cd safert-road-inclass
./gradlew bootRun
```

Windows:

```bat
cd safert-road-inclass
gradlew.bat bootRun
```

기본 포트: `8080` (`PORT` 환경변수로 오버라이드 가능)

## 프로파일/설정

- 기본 활성 프로파일: `dev` (`SPRING_PROFILES_ACTIVE`로 변경)
- 공통 설정: `src/main/resources/application.properties`
- 개발 설정: `src/main/resources/application-dev.properties`
- 운영 설정: `src/main/resources/application-prod.properties`

주요 환경변수:

- `SPRING_PROFILES_ACTIVE`: `dev` 또는 `prod`
- `PORT`: 서버 포트
- `CORS_ALLOWED_ORIGINS`: 허용 Origin 목록
- `GEMINI_API_KEY`: Gemini API Key
- `SUPABASE_URL`, `SUPABASE_SERVICE_ROLE_KEY`: Supabase 스토리지 연동

## 주요 API 그룹

- 인증: `/api/v1/auth/*`
- 건강 체크: `/api/v1/health/*`
- 체크리스트: `/api/v1/checklists/*`
- 템플릿: `/api/v1/templates/*`
- 위험도/조치: `/api/v1/risks/*`, `/api/v1/reviews/*`
- 포인트/활동: `/api/v1/users/me/points/*`, `/api/v1/users/me/activities`
- 출석/보상: `/api/v1/quests/attendance/*`, `/api/v1/attendance/rewards/*`
- 인벤토리: `/api/v1/inventory/*`
- 골드/교환/보상센터: `/api/v1/gold/*`, `/api/v1/exchange/*`, `/api/v1/rewards/*`
- 게임 프로필: `/api/v1/game-profile/me/*`
- 알림: `/api/v1/alerts/*`
- AI: `/api/v1/ai/*`, `/api/v1/business-plan/*`
- 파일: `/api/v1/files/*`

Swagger UI:

- `http://localhost:8080/swagger-ui`

## 프로젝트 구조

```text
safert-road-inclass/
├─ src/main/java/com/jinsung/safety_road_inclass/
│  ├─ domain/         # 도메인별 controller/service/repository/entity
│  └─ global/         # 보안, 공통 응답, 예외, 설정
├─ src/main/resources/
│  ├─ application.properties
│  ├─ application-dev.properties
│  └─ application-prod.properties
├─ sql/               # 스키마/데이터 SQL
└─ build.gradle
```

## 보안/인증 참고

- JWT 기반 Stateless 인증
- CORS 허용 Origin은 설정 파일/환경변수에서 관리
- 일부 엔드포인트는 현재 프론트 연동 단계 특성상 `permitAll` 설정 포함 (SecurityConfig 참조)

## 참고 문서

- `safert-road-inclass/docs/API_SPECIFICATION.md`
- `safert-road-inclass/docs/BACKEND_INTEGRATION_GUIDE.md`
- `safert-road-inclass/docs/CLOUDTYPE_DEPLOYMENT.md`
- `safert-road-inclass/docs/database.md`
- `safert-road-inclass/docs/백엔드_진행현황.md`

---

최종 업데이트: 2026-02-13


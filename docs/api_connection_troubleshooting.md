# API 연결 및 배포 트러블슈팅 기록

> **작성일:** 2026년 1월 10일  
> **프로젝트:** Safety Quest Game (Frontend) ↔ Safety Road InClass (Backend)  
> **목적:** 프론트엔드-백엔드 API 연결 및 배포 문제 해결 과정 종합 정리

---

## 📋 프로젝트 개요

| 구분 | 프로젝트명 | 배포 URL | 기술스택 |
|------|----------|---------|---------|
| Frontend | safety-quest-game | https://www.safety-road.online | React (Vite) |
| Backend | safert-road-inclass | CloudType 배포 | Spring Boot + SQLite |

---

## 🔴 발생한 문제들

### 1. Mixed Content 에러

**문제 현상:**
```
Mixed Content: The page at 'https://www.safety-road.online' was loaded over HTTPS, 
but requested an insecure resource 'http://srt-road-inclass-....'
```

**해결:** `environment.js`에서 API URL을 자동으로 HTTPS로 변환
```javascript
API_BASE_URL: (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080')
    .replace(/^http:\/\/(?!localhost|127\.0\.0\.1)/, 'https://'),
```

---

### 2. CORS 에러

**문제 현상:**
```
Access to fetch at 'https://...' from origin 'https://www.safety-road.online' 
has been blocked by CORS policy
```

**해결:** `SecurityConfig.java`에서 환경변수로 CORS Origin 관리
```properties
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:https://www.safety-road.online}
```

---

### 3. gradlew: not found 에러 (CloudType 빌드 실패)

**문제 현상:**
```
/bin/sh: 1: ./gradlew: not found
```

**원인:** Windows CRLF 줄바꿈이 Linux에서 실행 불가

**해결:** `.gitattributes` 수정으로 gradlew를 LF로 강제 변환
```gitattributes
gradlew text eol=lf
gradlew.bat text eol=crlf
```

---

### 4. 503 서비스 중지 에러

**원인:** 데이터베이스 연결 실패 또는 환경변수 누락

**해결:** MySQL → SQLite로 전환하여 외부 DB 의존성 제거

---

## 🔄 주요 변경 사항 (SQLite 전환)

### 변경된 의존성 (`build.gradle`)
```diff
- runtimeOnly 'com.mysql:mysql-connector-j'
- runtimeOnly 'com.h2database:h2'
+ runtimeOnly 'org.xerial:sqlite-jdbc:3.45.1.0'
+ implementation 'org.hibernate.orm:hibernate-community-dialects:6.4.4.Final'
```

### 개발 환경 (`application-dev.properties`)
```properties
spring.datasource.url=jdbc:sqlite:./data/safetyroad-dev.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
```

### 운영 환경 (`application-prod.properties`)
```properties
spring.datasource.url=jdbc:sqlite:./data/safetyroad.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
```

---

## 🔧 CloudType 배포 설정

### 배포 타입: **Java**

| 항목 | 값 |
|------|-----|
| Type | Java |
| Java Version | 21 |
| Build Tool | Gradle |
| Health Check | `/api/v1/health/ping` |

### 필수 환경변수

| 환경변수명 | 설명 | 예시 값 |
|-----------|------|--------|
| `SPRING_PROFILES_ACTIVE` | 활성 프로파일 | `prod` |
| `CORS_ALLOWED_ORIGINS` | 프론트엔드 도메인 | `https://www.safety-road.online` |
| `GEMINI_API_KEY` | AI API 키 | `AIza...` |

> ⚠️ SQLite 전환으로 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 환경변수는 더 이상 불필요

---

## 📝 배운 점

1. **HTTPS/HTTP 혼합 불가** - 브라우저 보안 정책
2. **CORS 설정 필수** - 프론트엔드 도메인을 백엔드에서 명시적 허용
3. **줄바꿈 문제** - Windows CRLF → Linux LF 변환 필요
4. **SQLite 장점** - 외부 DB 서버 불필요, 배포 간편

---

## 🔗 관련 파일 경로

| 파일 | 경로 |
|-----|------|
| Frontend 환경설정 | `safety-quest-game/src/config/environment.js` |
| Backend Security | `safert-road-inclass/.../global/config/SecurityConfig.java` |
| Backend 개발 설정 | `safert-road-inclass/src/main/resources/application-dev.properties` |
| Backend 운영 설정 | `safert-road-inclass/src/main/resources/application-prod.properties` |
| Gradle 빌드 | `safert-road-inclass/build.gradle` |

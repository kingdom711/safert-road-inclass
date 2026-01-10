# API 연결 트러블슈팅 기록

> **작성일:** 2026년 1월 10일  
> **프로젝트:** Safety Quest Game (Frontend) ↔ Safety Road InClass (Backend)  
> **목적:** 프론트엔드-백엔드 API 연결 문제 해결 과정 정리

---

## 📋 프로젝트 개요

| 구분 | 프로젝트명 | 배포 URL | 기술스택 |
|------|----------|---------|---------|
| Frontend | safety-quest-game | https://www.safety-road.online | React (Vite) |
| Backend | safert-road-inclass | CloudType 배포 | Spring Boot |

---

## 🔴 발생한 문제들

### 1. Mixed Content 에러

**문제 현상:**
```
Mixed Content: The page at 'https://www.safety-road.online' was loaded over HTTPS, 
but requested an insecure resource 'http://srt-road-inclass-....'
```

**원인:**
- 프론트엔드가 HTTPS로 배포되었지만, API 요청은 HTTP로 전송
- 브라우저 보안 정책상 HTTPS 페이지에서 HTTP 리소스 요청 차단

**해결 방법:**
- `environment.js`에서 API URL을 자동으로 HTTPS로 변환하는 로직 추가

```javascript
// environment.js - 수정된 코드
API_BASE_URL: (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080')
    .replace(/^http:\/\/(?!localhost|127\.0\.0\.1)/, 'https://'),
```

- localhost가 아닌 경우 자동으로 `http://` → `https://` 변환

---

### 2. CORS (Cross-Origin Resource Sharing) 에러

**문제 현상:**
```
Access to fetch at 'https://...' from origin 'https://www.safety-road.online' 
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header
```

**원인:**
- 백엔드 서버의 CORS 설정에 프론트엔드 도메인이 허용되지 않음
- CloudType 환경변수 `CORS_ALLOWED_ORIGINS`에 프론트엔드 URL 누락

**해결 방법:**

1. **백엔드 `SecurityConfig.java`** - CORS 설정 확인
```java
@Value("${cors.allowed-origins}")
private java.util.List<String> allowedOrigins;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // 환경변수에서 허용된 Origin 목록 로드
    configuration.setAllowedOrigins(allowedOrigins);
    
    // 허용할 HTTP 메서드
    configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    
    // 인증 정보 포함 허용 (JWT 토큰 전송용)
    configuration.setAllowCredentials(true);
    
    // ...
}
```

2. **`application.properties`** - 허용 Origin 설정
```properties
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173,https://www.safety-road.online}
```

3. **CloudType 환경변수 설정** (배포 시)
```
CORS_ALLOWED_ORIGINS=https://www.safety-road.online
```

---

### 3. Connection Timeout / 서버 연결 실패

**문제 현상:**
- 서버에 연결할 수 없습니다.
- 요청 시간 초과 (504 Timeout)

**원인:**
- CloudType 서버가 일시적으로 슬립 상태
- 백엔드 서버 환경변수 설정 오류

**해결 방법:**
- 서버 상태 확인: `/api/v1/health/ping` 엔드포인트로 헬스체크
- 환경변수 재확인 및 서비스 재시작

---

## ✅ 현재 설정 상태

### Frontend (`environment.js`)
```javascript
const config = {
    // API 서버 URL (로컬호스트가 아니면 강제로 HTTPS 적용)
    API_BASE_URL: (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080')
        .replace(/^http:\/\/(?!localhost|127\.0\.0\.1)/, 'https://'),
    
    // Mock 모드 설정
    USE_MOCK: import.meta.env.VITE_USE_MOCK === 'true',
    
    // API 타임아웃 (30초)
    API_TIMEOUT: parseInt(import.meta.env.VITE_API_TIMEOUT) || 30000,
};
```

### Backend (`application.properties`)
```properties
# CORS 허용 Origin 목록
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:5173,https://www.safety-road.online}
```

### Backend (`SecurityConfig.java`)
- CORS 설정이 `application.properties`의 `cors.allowed-origins` 값을 읽어서 적용
- Preflight 요청(OPTIONS) 처리 포함
- JWT 토큰 전송을 위한 `allowCredentials(true)` 설정

---

## 🔧 CloudType Java 빌드 배포 설정

### 배포 타입 선택
> **Java 빌드** (Dockerfile 아님)를 선택하세요!

### 빌드 설정
| 항목 | 값 |
|------|-----|
| **Type** | Java |
| **Java Version** | 21 |
| **Build Tool** | Gradle |
| **Build Command** | `./gradlew clean build -x test` |

### 필수 환경변수

| 환경변수명 | 설명 | 예시 값 |
|-----------|------|--------|
| `SPRING_PROFILES_ACTIVE` | 활성 프로파일 | `prod` |
| `CORS_ALLOWED_ORIGINS` | 허용할 프론트엔드 도메인 | `https://www.safety-road.online` |
| `DB_URL` | MySQL 연결 URL | `jdbc:mysql://...` |
| `DB_USERNAME` | DB 사용자명 | `root` |
| `DB_PASSWORD` | DB 비밀번호 | `****` |
| `GEMINI_API_KEY` | AI API 키 | `AIza...` |

### 삭제한 파일 (충돌 방지)
- ~~`Dockerfile`~~ - Docker 배포용 (삭제됨)
- ~~`package-lock.json`~~ - Node.js 관련 불필요 (삭제됨)

---

## 📝 배운 점 & 질문 사항

### 배운 점
1. **HTTPS와 HTTP 혼합 사용 불가**: 보안상 HTTPS 페이지에서 HTTP API 호출 불가
2. **CORS 설정의 중요성**: 프론트엔드 도메인을 백엔드에서 명시적으로 허용해야 함
3. **환경변수 분리**: 개발/운영 환경별로 설정을 분리하는 것이 중요

### 강사님께 질문
1. **CORS 설정 방식**: 현재 환경변수로 CORS Origin을 관리하고 있는데, 더 나은 방법이 있을까요?
2. **HTTPS 강제 변환**: 프론트엔드에서 URL을 강제로 HTTPS로 변환하는 방식이 적절한가요?
3. **에러 핸들링**: 네트워크 에러와 서버 에러를 구분해서 사용자에게 안내하는 더 좋은 패턴이 있을까요?

---

## 🔗 관련 파일 경로

| 파일 | 경로 |
|-----|------|
| Frontend 환경설정 | `safety-quest-game/src/config/environment.js` |
| Frontend API 클라이언트 | `safety-quest-game/src/api/apiClient.js` |
| Backend Security 설정 | `safert-road-inclass/.../global/config/SecurityConfig.java` |
| Backend 공통 설정 | `safert-road-inclass/src/main/resources/application.properties` |
| Backend 운영 설정 | `safert-road-inclass/src/main/resources/application-prod.properties` |

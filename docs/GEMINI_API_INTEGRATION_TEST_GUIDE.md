# Gemini API 통합 테스트 가이드

## 개요

Spring AI를 활성화한 후 실제 Gemini API 호출을 테스트하는 통합 테스트 코드가 작성되었습니다.

## 현재 상태

### ✅ 완료된 작업

1. **테스트 코드 작성 완료**
   - `DocumentEnhancementServiceIntegrationTest.java`: 실제 Gemini API 호출 단위 테스트
   - `AiControllerIntegrationTest.java`: 실제 Gemini API 호출 통합 테스트
   - `application-test.properties`: 테스트 전용 설정 파일

2. **코드 구조 준비 완료**
   - `DocumentEnhancementService`: Spring AI ChatModel 사용 코드 활성화
   - Fallback 메커니즘 구현 (ChatModel이 없을 때 자동 Fallback)

### ⚠️ 해결 필요 사항

**Spring AI API 호환성 문제**: 
- 의존성 `spring-ai-vertex-ai-gemini`는 해결되었습니다.
- 하지만 Spring AI 1.0.0의 API가 예상과 다를 수 있습니다 (`ChatResponse`, `ChatResponseMetadata` 클래스를 찾을 수 없음).
- Spring AI 1.0.0의 실제 API 문서를 확인하여 코드를 수정해야 할 수 있습니다.

## Spring AI 의존성 해결 방법

### 방법 1: 의존성 확인 (해결됨)

Spring AI 1.0.0에서 Google Gemini를 사용하는 올바른 의존성:

```gradle
// Spring AI 1.0.0 BOM을 통해 버전 관리
dependencyManagement {
    imports {
        mavenBom 'org.springframework.ai:spring-ai-bom:1.0.0'
    }
}

dependencies {
    // Spring AI 1.0.0에서는 spring-ai-vertex-ai-gemini 사용
    implementation 'org.springframework.ai:spring-ai-vertex-ai-gemini'
}
```

**현재 상태**: 의존성은 해결되었지만, Spring AI 1.0.0의 API가 예상과 다를 수 있습니다.

### 방법 2: Maven Central에서 확인

1. [Maven Central](https://search.maven.org/)에서 검색
2. `org.springframework.ai` 그룹 ID로 검색
3. `spring-ai-*-gemini-*` 또는 `spring-ai-*-google-*` 패턴으로 검색
4. 정확한 artifactId 확인

### 방법 3: Spring AI 공식 문서 확인

Spring AI 1.0.0 공식 문서에서 Google Gemini 통합 방법 확인:
- [Spring AI Reference Documentation](https://docs.spring.io/spring-ai/reference/)

## 테스트 실행 방법

### 1. 환경 변수 설정

```bash
# Windows PowerShell
$env:GEMINI_API_KEY="your_api_key_here"

# Windows CMD
set GEMINI_API_KEY=your_api_key_here

# Linux/Mac
export GEMINI_API_KEY=your_api_key_here
```

### 2. 테스트 실행

#### 전체 테스트 실행 (통합 테스트 제외)
```bash
cd backend
./gradlew test
```

#### 통합 테스트만 실행 (GEMINI_API_KEY 필요)
```bash
cd backend
./gradlew test --tests "*IntegrationTest" --tests "*gemini-api"
```

#### IDE에서 실행
- IntelliJ IDEA: 각 테스트 클래스 우클릭 → Run Tests
- Eclipse: 각 테스트 클래스 우클릭 → Run As → JUnit Test

### 3. 테스트 태그 설명

- `@Tag("integration")`: 통합 테스트 태그
- `@Tag("gemini-api")`: Gemini API 호출 테스트 태그
- `@EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY")`: 환경변수가 있을 때만 실행

## 테스트 코드 구조

### DocumentEnhancementServiceIntegrationTest

**위치**: `backend/src/test/java/com/jinsung/safety_road_inclass/domain/ai/service/DocumentEnhancementServiceIntegrationTest.java`

**테스트 케이스**:
1. `enhanceDocument_RealApiCall_Success`: 실제 API 호출 성공 테스트
2. `enhanceDocument_RealApiCall_LongContent`: 긴 문서 보강 테스트
3. `enhanceDocument_RealApiCall_TechnicalTerms`: 전문 용어 포함 문서 테스트
4. `enhanceDocument_RealApiCall_NullInput`: null 입력 처리 테스트
5. `enhanceDocument_RealApiCall_EmptyString`: 빈 문자열 처리 테스트
6. `enhanceDocument_RealApiCall_TokenUsage`: 토큰 사용량 측정 검증

### AiControllerIntegrationTest

**위치**: `backend/src/test/java/com/jinsung/safety_road_inclass/domain/ai/controller/AiControllerIntegrationTest.java`

**테스트 케이스**:
1. `analyzeText_RealApiCall_IntegrationTest`: REST API 엔드포인트 통합 테스트
2. `analyzeText_RealApiCall_LongContent`: 긴 문서 처리 통합 테스트

## 테스트 설정 파일

### application-test.properties

**위치**: `backend/src/test/resources/application-test.properties`

**주요 설정**:
- Spring AI Gemini 설정
- H2 인메모리 데이터베이스 설정
- 테스트용 로깅 레벨

## 다음 단계

1. ✅ **Spring AI 의존성 해결**: `spring-ai-vertex-ai-gemini` 의존성 추가 완료
2. ⚠️ **Spring AI API 호환성 확인**: Spring AI 1.0.0의 실제 API 문서 확인 필요
   - `ChatResponse`, `ChatResponseMetadata` 클래스가 다른 패키지에 있을 수 있음
   - Spring AI 1.0.0 공식 문서 참조: https://docs.spring.io/spring-ai/reference/
3. **코드 수정**: API가 다를 경우 `DocumentEnhancementService` 코드 수정
4. **의존성 활성화 후 테스트 실행**: `GEMINI_API_KEY` 환경변수 설정 후 테스트 실행
5. **CI/CD 통합**: 통합 테스트를 CI/CD 파이프라인에 추가 (선택적 실행)

## 참고사항

- 통합 테스트는 실제 API 호출을 수행하므로 비용이 발생할 수 있습니다
- 네트워크 연결이 필요합니다
- 테스트 실행 시간이 길어질 수 있습니다 (API 응답 대기)
- `@EnabledIfEnvironmentVariable`로 환경변수가 없으면 자동으로 스킵됩니다

## 문제 해결

### 의존성 해결 실패 시

1. Spring AI BOM 버전 확인: `build.gradle`의 `spring-ai-bom:1.0.0` 확인
2. Gradle 캐시 정리: `./gradlew clean --refresh-dependencies`
3. Spring AI 버전 변경: 최신 버전으로 업데이트 시도

### 테스트 실행 실패 시

1. `GEMINI_API_KEY` 환경변수 확인
2. 네트워크 연결 확인
3. API 키 유효성 확인
4. 로그 확인: `application-test.properties`의 로깅 레벨 조정

---

**작성일**: 2024년  
**버전**: 1.0


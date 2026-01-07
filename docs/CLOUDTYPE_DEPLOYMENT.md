# CloudType 배포 가이드

## 수정 사항 요약

CloudType 배포 시 발생하는 503 에러를 해결하기 위해 다음과 같은 수정을 진행했습니다:

1. **포트 설정**: `PORT` 환경 변수 지원 추가
2. **프로덕션 프로파일**: `application-prod.properties` 생성
3. **헬스체크**: 루트 경로(`/`) 헬스체크 엔드포인트 추가
4. **보안 설정**: SecurityConfig에서 루트 경로 허용

---

## CloudType 환경 변수 설정

CloudType 대시보드에서 다음 환경 변수를 설정해야 합니다:

### 필수 환경 변수

| 변수명 | 설명 | 예시 값 |
|--------|------|---------|
| `SPRING_PROFILES_ACTIVE` | 활성 프로파일 | `prod` |
| `PORT` | 서버 포트 (자동 설정됨) | CloudType이 자동 설정 |
| `DB_URL` | 데이터베이스 연결 URL | `jdbc:mysql://host:3306/safetyroad?useSSL=false&serverTimezone=Asia/Seoul` |
| `DB_USERNAME` | 데이터베이스 사용자명 | `root` |
| `DB_PASSWORD` | 데이터베이스 비밀번호 | `your_password` |
| `GEMINI_API_KEY` | Google Gemini API 키 | `your_api_key` |

### 선택적 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `JWT_SECRET` | JWT 시크릿 키 (프로덕션에서는 변경 권장) | 개발용 기본값 사용 |

---

## 배포 단계

### 1. 환경 변수 설정

CloudType 대시보드에서:
1. 프로젝트 설정 → 환경 변수 메뉴로 이동
2. 위의 환경 변수들을 추가
3. 데이터베이스 연결 정보 확인 (CloudType이 제공하는 경우 해당 값 사용)

### 2. 빌드 및 배포

```bash
# 로컬에서 빌드 테스트
./gradlew clean build

# 빌드된 JAR 파일 위치
# build/libs/safety-road-inclass-0.0.1-SNAPSHOT.jar
```

CloudType은 자동으로 빌드 및 배포를 수행합니다.

### 3. 헬스체크 확인

배포 후 다음 엔드포인트로 서버 상태를 확인할 수 있습니다:

- **루트 경로**: `https://your-domain.cloudtype.app/`
- **상세 헬스체크**: `https://your-domain.cloudtype.app/api/v1/health`
- **간단 헬스체크**: `https://your-domain.cloudtype.app/api/v1/health/ping`

---

## 문제 해결

### 503 에러가 계속 발생하는 경우

1. **환경 변수 확인**
   - `SPRING_PROFILES_ACTIVE=prod` 설정 확인
   - 데이터베이스 연결 정보 확인

2. **로그 확인**
   - CloudType 대시보드에서 애플리케이션 로그 확인
   - 데이터베이스 연결 오류 확인

3. **포트 확인**
   - `PORT` 환경 변수가 올바르게 설정되었는지 확인
   - CloudType이 자동으로 설정하는 경우 별도 설정 불필요

4. **데이터베이스 연결**
   - MySQL 데이터베이스가 실행 중인지 확인
   - 연결 정보(호스트, 포트, 사용자명, 비밀번호) 확인
   - 방화벽 설정 확인

### 애플리케이션이 시작되지 않는 경우

1. **Java 버전 확인**
   - Java 21이 설치되어 있는지 확인
   - `build.gradle`에서 Java 21 요구사항 확인

2. **의존성 확인**
   - `build.gradle`의 모든 의존성이 올바르게 다운로드되었는지 확인
   - 네트워크 연결 확인

3. **프로파일 확인**
   - `SPRING_PROFILES_ACTIVE` 환경 변수가 올바르게 설정되었는지 확인

---

## 프로파일별 설정

### 개발 환경 (dev)
- H2 인메모리 데이터베이스 사용
- 자동 테이블 생성/삭제
- SQL 로그 출력
- H2 콘솔 활성화

### 프로덕션 환경 (prod)
- MySQL 데이터베이스 사용
- Flyway를 통한 스키마 마이그레이션
- SQL 로그 비활성화
- H2 콘솔 비활성화

---

## 추가 참고사항

- CloudType은 자동으로 `PORT` 환경 변수를 제공합니다
- 데이터베이스 연결 정보는 CloudType이 제공하는 경우 해당 값을 사용하세요
- 프로덕션 환경에서는 JWT 시크릿 키를 변경하는 것을 권장합니다
- CORS 설정은 필요에 따라 프로덕션 도메인을 추가하세요


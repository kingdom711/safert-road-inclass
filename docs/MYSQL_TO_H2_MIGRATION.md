# MySQL → H2 데이터베이스 전환 작업 요약

> 작성일: 2026-01-08  
> 목적: 로컬 개발 환경에서 MySQL 없이 빠른 테스트 및 디버깅을 위한 H2 인메모리 데이터베이스 사용

---

## 1. 프로파일 기반 데이터베이스 전환 아키텍처

| 구분 | 개발 환경 (dev) | 운영 환경 (prod) |
|------|----------------|------------------|
| **데이터베이스** | H2 In-Memory | MySQL |
| **드라이버** | `org.h2.Driver` | `com.mysql.cj.jdbc.Driver` |
| **DDL 전략** | `create-drop` | `validate` |
| **Dialect** | `H2Dialect` | `MySQLDialect` |
| **Flyway** | 비활성화 | 활성화 |
| **H2 Console** | 활성화 | 비활성화 |

---

## 2. 설정 파일 구조

```
src/main/resources/
├── application.properties          # 공통 설정 (기본 프로파일: dev)
├── application-dev.properties      # H2 개발 환경 설정
└── application-prod.properties     # MySQL 운영 환경 설정
```

---

## 3. 핵심 설정 내용

### 3.1 `application.properties` (공통)

```properties
# Active Profile (dev: H2, mysql: MySQL)
# CloudType에서는 prod 프로파일 사용 (환경 변수로 설정 가능)
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}
```

- **기본 프로파일**: `dev` (H2 사용)
- 환경변수 `SPRING_PROFILES_ACTIVE`로 전환 가능

### 3.2 `application-dev.properties` (개발 - H2)

```properties
# H2 In-Memory Database
spring.datasource.url=jdbc:h2:mem:safetyroad;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# H2 Console (http://localhost:8080/h2-console)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.h2.console.settings.web-allow-others=false

# JPA / Hibernate - Development Settings
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Flyway - Disabled for Development (using Hibernate ddl-auto)
spring.flyway.enabled=false
```

### 3.3 `application-prod.properties` (운영 - MySQL)

```properties
# MySQL Database (환경 변수로 설정)
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/safetyroad?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}

# Connection Pool (HikariCP)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000

# H2 Console - 프로덕션에서는 비활성화
spring.h2.console.enabled=false

# JPA / Hibernate - Production Settings
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect

# Flyway - Enabled for Production (Schema Migration)
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

---

## 4. 보안 설정 (SecurityConfig.java)

```java
// H2 Console을 위한 Frame 허용 (개발 환경)
.headers(headers -> headers
        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

// URL 기반 접근 제어
.authorizeHttpRequests(auth -> auth
        ...
        .requestMatchers("/h2-console/**").permitAll()
        ...
```

- H2 Console iframe 허용 (`sameOrigin`)
- `/h2-console/**` 경로 인증 없이 접근 허용

---

## 5. H2 데이터베이스 사용 시 장점

| 장점 | 설명 |
|------|------|
| **빠른 시작** | MySQL 설치/설정 불필요 |
| **자동 스키마 생성** | `create-drop`으로 매번 새로 생성 |
| **웹 콘솔** | `http://localhost:8080/h2-console`에서 DB 직접 조회 |
| **MySQL 호환 모드** | `MODE=MySQL`로 MySQL 문법 호환 |
| **개발 속도 향상** | 테스트/디버깅 시 빠른 피드백 |

---

## 6. 실행 방법

```bash
# 개발 환경 (H2) - 기본
./gradlew bootRun

# 또는 명시적으로 dev 프로파일 지정
./gradlew bootRun --args='--spring.profiles.active=dev'

# 운영 환경 (MySQL)
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### Windows 환경

```powershell
# 개발 환경 (H2) - 기본
.\gradlew.bat bootRun

# 운영 환경 (MySQL)
.\gradlew.bat bootRun --args='--spring.profiles.active=prod'
```

---

## 7. H2 Console 접속 정보

| 항목 | 값 |
|------|-----|
| **URL** | `http://localhost:8080/h2-console` |
| **JDBC URL** | `jdbc:h2:mem:safetyroad` |
| **Username** | `sa` |
| **Password** | (빈 값) |

### H2 Console 접속 화면

1. 브라우저에서 `http://localhost:8080/h2-console` 접속
2. JDBC URL에 `jdbc:h2:mem:safetyroad` 입력
3. Username: `sa`, Password: 빈칸
4. Connect 클릭

---

## 8. 의존성 (build.gradle)

```groovy
dependencies {
    // Database
    runtimeOnly 'com.mysql:mysql-connector-j'
    runtimeOnly 'com.h2database:h2'
    
    // Flyway (Database Migration)
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-mysql'
}
```

- 두 데이터베이스 드라이버 모두 `runtimeOnly`로 포함
- 프로파일에 따라 적절한 드라이버 자동 선택

---

## 9. 주의사항

### 9.1 H2 인메모리 DB 특성

- **서버 재시작 시 데이터 초기화**: 모든 데이터가 메모리에만 저장되므로 서버 종료 시 삭제됨
- **테스트 데이터**: 필요 시 `data.sql` 또는 `@PostConstruct`로 초기 데이터 삽입

### 9.2 MySQL과의 차이점

- 일부 MySQL 전용 함수나 문법이 H2에서 동작하지 않을 수 있음
- `MODE=MySQL` 설정으로 대부분 호환되지만 완벽하지 않음
- 운영 배포 전 반드시 MySQL 환경에서 테스트 필요

### 9.3 프로파일 전환 시 체크리스트

- [ ] 환경변수 `SPRING_PROFILES_ACTIVE` 확인
- [ ] MySQL 사용 시 DB 서버 실행 여부 확인
- [ ] Flyway 마이그레이션 스크립트 최신화 확인

---

## 10. 관련 파일 목록

| 파일 | 설명 |
|------|------|
| `application.properties` | 공통 설정 및 기본 프로파일 지정 |
| `application-dev.properties` | H2 개발 환경 설정 |
| `application-prod.properties` | MySQL 운영 환경 설정 |
| `SecurityConfig.java` | H2 Console 보안 설정 |
| `build.gradle` | 데이터베이스 드라이버 의존성 |

---

이 구조를 통해 **개발 시에는 H2로 빠르게 테스트**하고, **배포 시에는 MySQL로 안정적으로 운영**할 수 있습니다! 🚀

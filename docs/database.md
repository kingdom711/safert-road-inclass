# 📊 MySQL → H2 데이터베이스 변경 작업 체계적 요약

## 1. 변경 개요

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 데이터베이스 | MySQL (외부 설치 필요) | H2 In-Memory (내장) |
| 구성 방식 | 단일 설정 | **프로파일 기반 분리** |
| 개발 환경 | MySQL 설치 필수 | **별도 설치 불필요** |
| 데이터 지속성 | 영구 저장 | 메모리 (서버 종료시 삭제) |

---

## 2. 파일별 변경 내역

### 📁 2.1 `build.gradle` - 의존성 추가

```gradle
// Database
runtimeOnly 'com.mysql:mysql-connector-j'
runtimeOnly 'com.h2database:h2'
```

**변경 사항**: H2 데이터베이스 드라이버 추가 (MySQL과 공존)

---

### 📁 2.2 `application.properties` - 공통 설정 + 프로파일 지정

```properties
# Active Profile (dev: H2, mysql: MySQL)
spring.profiles.active=dev
```

**변경 사항**: 
- 데이터베이스 설정을 **프로파일별로 분리**
- 기본 활성 프로파일을 `dev` (H2)로 설정

---

### 📁 2.3 `application-dev.properties` - H2 설정 (신규 생성)

```properties
# ===========================================
# Safety Road - Development Profile (H2)
# ===========================================

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

# Security - Disable CSRF for H2 Console
# (Configured in SecurityConfig.java)
```

**주요 설정 설명**:

| 설정 | 값 | 설명 |
|------|-----|------|
| `datasource.url` | `jdbc:h2:mem:safetyroad` | 메모리 DB (휘발성) |
| `MODE=MySQL` | | MySQL 호환 모드 |
| `DB_CLOSE_DELAY=-1` | | 연결 없어도 DB 유지 |
| `h2.console.enabled` | `true` | 웹 콘솔 활성화 |
| `ddl-auto` | `create-drop` | 시작시 생성, 종료시 삭제 |
| `flyway.enabled` | `false` | 개발환경에서 비활성화 |

---

### 📁 2.4 `application-mysql.properties` - MySQL 설정 (프로덕션용 유지)

```properties
# ===========================================
# Safety Road - MySQL Profile (Production/Staging)
# ===========================================

# MySQL Database
spring.datasource.url=jdbc:mysql://localhost:3306/safetyroad?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=${DB_USERNAME:safetyroad}
spring.datasource.password=${DB_PASSWORD:safetyroad123}

# Connection Pool (HikariCP)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000

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

### 📁 2.5 `SecurityConfig.java` - H2 Console 접근 허용

```java
// H2 Console을 위한 Frame 허용 (개발 환경)
.headers(headers -> headers
    .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
)

// URL 기반 접근 제어
.authorizeHttpRequests(auth -> auth
    // Public endpoints (인증 불필요)
    .requestMatchers("/api/v1/auth/**").permitAll()
    .requestMatchers("/api/v1/health").permitAll()
    .requestMatchers("/h2-console/**").permitAll()
    // ...
)
```

**변경 사항**:
1. `frameOptions(sameOrigin)` - H2 콘솔이 iframe으로 동작하므로 필요
2. `/h2-console/**` 경로를 `permitAll()`로 인증 없이 접근 허용

---

## 3. 프로파일별 비교표

| 설정 항목 | dev (H2) | mysql (MySQL) |
|-----------|----------|---------------|
| **데이터베이스** | H2 In-Memory | MySQL 8.x |
| **JDBC URL** | `jdbc:h2:mem:safetyroad` | `jdbc:mysql://localhost:3306/safetyroad` |
| **드라이버** | `org.h2.Driver` | `com.mysql.cj.jdbc.Driver` |
| **DDL 자동화** | `create-drop` | `validate` |
| **SQL 출력** | `true` | `false` |
| **Flyway** | 비활성화 | 활성화 |
| **H2 콘솔** | 활성화 | - |
| **용도** | 개발/테스트 | 운영/스테이징 |

---

## 4. 환경별 실행 방법

### 🔹 개발 환경 (H2) - 기본값
```bash
# Windows
cd backend
.\gradlew.bat bootRun

# Linux/Mac
cd backend
./gradlew bootRun
```

### 🔹 운영 환경 (MySQL)
```bash
# 방법 1: 명령줄 인자
.\gradlew.bat bootRun --args='--spring.profiles.active=mysql'

# 방법 2: 환경변수
$env:SPRING_PROFILES_ACTIVE="mysql"
.\gradlew.bat bootRun
```

---

## 5. H2 Console 접속 정보

| 항목 | 값 |
|------|-----|
| 접속 URL | http://localhost:8080/h2-console |
| JDBC URL | `jdbc:h2:mem:safetyroad` |
| Username | `sa` |
| Password | (비워두기) |

---

## 6. 변경의 장점

| 장점 | 설명 |
|------|------|
| ✅ **즉시 개발 가능** | MySQL 설치 없이 바로 개발/테스트 시작 |
| ✅ **빠른 시작** | 인메모리 DB로 빠른 부팅 (~3초) |
| ✅ **깨끗한 상태** | 매 실행시 초기화 (테스트에 유리) |
| ✅ **웹 콘솔** | H2 Console로 DB 직접 확인 가능 |
| ✅ **환경 분리** | 프로파일로 개발/운영 환경 명확히 구분 |
| ✅ **MySQL 호환** | `MODE=MySQL` 설정으로 호환성 유지 |

---

## 7. 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────────┐
│                    Spring Boot Application                   │
├─────────────────────────────────────────────────────────────┤
│  application.properties                                      │
│  └── spring.profiles.active=dev                             │
│                    ↓                                         │
│  ┌──────────────────────┬──────────────────────┐            │
│  │   dev Profile        │   mysql Profile      │            │
│  │   (기본 활성화)       │   (프로덕션용)        │            │
│  ├──────────────────────┼──────────────────────┤            │
│  │ • H2 In-Memory       │ • MySQL 8.x          │            │
│  │ • ddl-auto: create   │ • ddl-auto: validate │            │
│  │ • H2 Console 활성    │ • Flyway 활성        │            │
│  │ • Flyway 비활성      │ • Connection Pool    │            │
│  └──────────────────────┴──────────────────────┘            │
└─────────────────────────────────────────────────────────────┘
```

---

이 구조로 **개발 시에는 H2로 빠르게 테스트**하고, **배포 시에는 프로파일만 변경하여 MySQL로 전환**할 수 있습니다! 🎉


# Safety Road (안전의 길) Backend Service

산업 현장의 안전 관리를 디지털화하여 체크리스트 기반의 효율적인 안전 점검 시스템을 제공하는 백엔드 서비스입니다.
법정 자율점검표(사다리/고소/밀폐)의 전산화와 AI 기반 위험 분석을 지원하며, 견고한 데이터 무결성과 안정적인 트랜잭션 처리를 보장합니다.

## 🏗 Project Architecture

### Core Technology Stack
- **Language**: Java 17 (LTS)
- **Framework**: Spring Boot 3.x
- **Build Tool**: Gradle 8.x (Groovy DSL)
- **Database**: MySQL 8.x
- **ORM**: Spring Data JPA
- **Security**: Spring Security + JWT
- **API Specs**: OpenAPI 3.0 (Swagger)

### Layered Architecture
프로젝트는 **Layered Architecture** 패턴을 엄격히 준수합니다.
- **Controller Layer** (`api`): 요청 검증, 서비스 호출, DTO 변환
- **Service Layer** (`service/domain`): 비즈니스 로직, 트랜잭션 관리
- **Repository Layer** (`repository`): 데이터 액세스 (JPA)
- **Infrastructure Layer** (`infra`): 외부 시스템 연동 (S3, AI API)

---

## 🚀 Key Features (MVP)

1.  **Authentication & RBAC**
    *   JWT 기반 인증 시스템
    *   역할별 권한 관리 (기술인, 관리감독자, 안전관리자)

2.  **Digital Checklist System**
    *   작업 유형별(사다리, 고소작업 등) 템플릿 로딩
    *   체크리스트 작성, 임시 저장, 최종 제출 및 승인 워크플로우

3.  **Risk Management & Analysis**
    *   '아니요' 응답 항목에 대한 위험도 자동 평가
    *   Python 기반 AI 분석 모듈 연동 (위험도 예측)

4.  **Action Tracking**
    *   부적합 항목에 대한 조치 내역(텍스트, 사진) 기록
    *   AWS S3를 이용한 안전 점검 이미지 스토리지

---

## 🛠 Environment Setup & Build

### Prerequisites
- **JDK**: OpenJDK 21 (프로젝트는 Java 17 문법 호환)
- **Database**: MySQL 8.x
- **IDE**: VS Code or IntelliJ IDEA (with Lombok plugin)

### Local Development Setup
1. **Repository Clone**
   ```bash
   git clone https://github.com/kingdom711/safert-road-inclass.git
   cd safert-road-inclass
   ```

2. **Database Configuration**
   - MySQL 실행 및 데이터베이스 생성
   ```sql
   CREATE DATABASE safety_road CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
   - `src/main/resources/application.properties` (또는 `application-local.yml`) 설정
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/safety_road
   spring.datasource.username={YOUR_USERNAME}
   spring.datasource.password={YOUR_PASSWORD}
   ```

3. **Build & Run**
   ```bash
   # Build
   ./gradlew clean build

   # Run
   ./gradlew bootRun
   ```

---

## 📂 Project Structure

```
src/main/java/com/jinsung/safety_road_inclass/
├── common/            # Global Configs, Exceptions, Utils
├── domain/            # Domain Entities & Business Logic
│   ├── auth/          # User, Role, Token
│   ├── checklist/     # Template, Instance, Item
│   ├── risk/          # Risk Assessment
│   └── action/        # Action Records
├── api/               # Rest Controllers (Web Layer)
└── infra/             # External Integrations (S3, AI)
```

---

## 📏 Development Guidelines

### Branch Strategy (Git Flow)
- `master`: 배포 가능한 안정 버전
- `develop`: 개발 중인 버전
- `feature/{feature-name}`: 기능 개발 브랜치

### Code Convention
- **Naming**: Class(PascalCase), Method(camelCase), DB Tables(snake_case)
- **Response Format**: 모든 API는 표준 응답 포맷을 준수합니다.
  ```json
  {
    "status": "success",
    "code": 200,
    "message": "Operation successful",
    "data": { ... }
  }
  ```
- **Commit Message**: Conventional Commits 준수 (e.g., `feat: add login api`, `fix: resolve jwt timeout`)

---

## 📝 API Documentation
서버 실행 후 아래 주소에서 API 문서를 확인할 수 있습니다.
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`

---

## 📞 Contact & Support
- **Repository**: [GitHub Link](https://github.com/kingdom711/safert-road-inclass)


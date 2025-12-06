---
title: "[INIT] 프로젝트 초기화 및 CI/CD 환경 구성"
labels: ["setup", "devops"]
assignees: []
---

## 1. 배경 (Background)
프로젝트 개발을 시작하기 위한 기본 환경을 구성하고, 코드 저장소 및 빌드/배포 파이프라인의 기초를 마련해야 합니다.

## 2. 상세 작업 (Tasks)
- [x] **Backend 프로젝트 구조화**
    - [x] Spring Boot 프로젝트 생성 (완료)
    - [x] `/backend` 디렉토리로 이동 및 패키지 구조(global, domain) 생성 (완료)
    - [ ] `build.gradle` 의존성 점검 (Lombok, JPA, Web, Validation, Security)
    - [ ] DB 연결 설정 (`application.properties` - MySQL/H2)
- [x] **Frontend 프로젝트 초기화**
    - [x] Vite + React + TS 생성 (완료)
    - [x] TailwindCSS 설정 및 기본 테마 구성 (완료)
    - [x] Axios 및 폴더 구조(`features`, `components`, `pages`) 세팅 (완료)
- [ ] **Git 환경 구성**
    - [ ] `.gitignore` 점검 (Backend/Frontend 통합)
    - [ ] Branch 전략 수립 (main, develop, feature/*)

## 3. 완료 조건 (Acceptance Criteria)
- [ ] Backend 애플리케이션이 에러 없이 구동되어야 한다.
- [x] Frontend 애플리케이션이 구동되고 "Hello World"가 보여야 한다.
- [ ] DB 스키마 생성을 위한 설정이 완료되어야 한다.

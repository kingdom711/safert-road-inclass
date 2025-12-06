# MVP 요구사항 분석 및 매핑 (Step 1)

## 1. MVP 범위 REQ-FUNC 목록 (SRS 1.2, 3.x 참조)

| REQ ID | 기능명 | Actor | 관련 Epic | 비고 |
|---|---|---|---|---|
| **F-01** | 사용자 인증 및 권한 관리 | 기술인, 감독자, 안전관리자 | Epic 1 (공통) | JWT, RBAC |
| **F-02** | 작업유형 선택 및 템플릿 로딩 | 기술인 | Epic 1 (체크리스트) | 사다리/고소/밀폐 |
| **F-03** | 체크리스트 작성 및 제출 | 기술인 | Epic 1 (체크리스트) | 사진, 임시저장 |
| **F-04** | 감독자 검토 및 승인/반려 | 관리감독자 | Epic 1 (체크리스트) | 반려사유 |
| **F-05** | 안전관리자 위험현황 및 조치 | 안전관리자 | Epic 2 (리스크관리) | 필터링, 조치기록 |
| **F-06** | AI 작업사진 위험분석 | 시스템(AI) | Epic 2 (리스크관리) | Vision API |
| **F-07** | 알림 및 내 할 일 | 시스템 | Epic 1, 2 공통 | 심플 리스트 |

## 2. MVP 범위 REQ-NF 목록 (SRS 7.x 참조)

| REQ ID | 구분 | 내용 | 비고 |
|---|---|---|---|
| **NF-01** | 성능 | 동시 접속 50명, API 1~3초 | k6 테스트 |
| **NF-02** | 보안 | 파일 업로드 제한, JWT 필수 | Spring Security |
| **NF-03** | 가용성/로그 | 감사 로그, 에러 로그 | AOP 로깅 |
| **NF-04** | 인프라 | AWS, S3, RDS | Terraform/Console |

## 3. 엔티티 및 API 매핑 기초

| Task Group | 주요 엔티티 | 주요 API (Draft) |
|---|---|---|
| **Auth** | User | POST /api/auth/login |
| **Checklist** | WorkType, ChecklistTemplate, ChecklistInstance, ChecklistItemResponse, Photo | GET /api/templates, POST /api/checklists, GET /api/checklists/{id} |
| **Review** | ChecklistInstance (status update) | POST /api/checklists/{id}/approve, /reject |
| **Risk/Action** | ActionRecord, ActionPhoto, ChecklistInstance | GET /api/checklists (filter), POST /api/checklists/{id}/actions |
| **AI** | AIAnalysisResult | POST /api/ai/analyze-photo |

---

## 4. Task 추출 전략

1. **Epic 0 (PoC)**: FE 전용. 전체 UI 흐름을 잡는 '얇고 넓은' 프로토타입.
2. **Functional (MVP)**: REQ-FUNC 단위로 BE/FE 통합 또는 분리하여 구현.
3. **Non-Functional**: 보안, 로깅, 인프라 설정을 별도 Task로 정의.


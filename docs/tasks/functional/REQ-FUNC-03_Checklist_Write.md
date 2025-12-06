# REQ-FUNC-03: 체크리스트 작성 및 제출

## 1. 개요
- **목적**: 작업자가 입력한 체크리스트 데이터와 사진을 저장.
- **SRS 섹션**: 3.3 F-03
- **구성 요소**:
    - BE: 체크리스트 저장 API, 파일 업로드(S3) 처리
    - FE: 폼 데이터 패키징, Multipart 전송
    - DB: ChecklistInstance, ItemResponse, Photo

## 2. Task 상세 내용 (Machine Readable)

```yaml
task_id: "REQ-FUNC-03-WRITE"
title: "체크리스트 데이터 저장 및 사진 업로드 구현"
summary: >
  작성된 체크리스트 응답(JSON)과 작업 사진(Multipart File)을 받아 DB와 스토리지에 저장한다.
  필수 항목 검증 및 임시저장/제출 상태 구분을 처리한다.
type: "functional"
epic: "EPIC_1_CHECKLIST"
req_ids: ["F-03"]
component: ["backend", "database", "infra"]

context:
  apis:
    - "POST /api/checklists"
    - "POST /api/photos" (또는 통합 업로드)
  entities: ["ChecklistInstance", "ChecklistItemResponse", "Photo"]

inputs:
  description: "체크리스트 응답 맵, 사진 파일들, 메타정보(현장명 등)"

outputs:
  description: "생성된 체크리스트 ID"

steps_hint:
  - "DB: Instance, Response, Photo 테이블 설계"
  - "BE: S3(또는 Local Mock) 연동 파일 업로드 서비스 구현"
  - "BE: ChecklistService - 트랜잭션 내에서 Instance/Response 저장"
  - "BE: '아니요' 응답 시 risk_flag 자동 마킹 로직"
  - "FE: FormData 생성 및 API 호출 연동"

preconditions:
  - "REQ-FUNC-02-TEMPLATE (템플릿 ID 참조 필요)"
  - "AWS S3 버킷 또는 로컬 업로드 디렉토리 설정"

postconditions:
  - "제출 후 DB에 데이터가 적재되고, 상태가 SUBMITTED로 변경된다."
  - "사진 파일이 지정된 스토리지 경로에 저장된다."

dependencies: ["REQ-FUNC-02-TEMPLATE"]
parallelizable: false
estimated_effort: "L"
priority: "Must"
agent_profile: ["backend"]
```


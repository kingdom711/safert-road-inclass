# AI Agent Task Usage Guide

## 1. 개요
이 문서는 `tasks/` 디렉토리에 정의된 에이전틱 작업 정의서(Agentic Task Definitions)를 사용하여, AI 에이전트(Cursor, Windsurf 등)에게 개발 작업을 지시하는 방법을 안내합니다.

## 2. Task 정의서 구조 해석
각 Task 파일(.md)은 **사람을 위한 설명**과 **기계(Agent)를 위한 YAML**로 구성됩니다.

### 2.1 YAML 블록의 핵심 필드
- `task_id`: Task의 고유 식별자입니다. 의존성 관리에 사용됩니다.
- `steps_hint`: 구현해야 할 핵심 로직의 순서입니다. 에이전트의 "생각의 사슬(Chain of Thought)"을 유도합니다.
- `context`: 개발에 필요한 API, 엔티티, 시퀀스 다이어그램 등의 참조 정보를 제공합니다.
- `dependencies`: 이 작업을 시작하기 전에 반드시 완료되어야 할 선행 Task ID입니다.

## 3. 에이전트 사용 패턴

### 패턴 A: 단일 Task 구현 지시 (Basic)
가장 기본적인 사용법입니다. 하나의 Task 파일을 열고 에이전트에게 지시합니다.

**Prompt 예시:**
> "@REQ-FUNC-01_Auth.md 파일을 읽고, 정의된 'REQ-FUNC-01-AUTH' Task를 수행해줘. YAML에 명시된 inputs, outputs, steps_hint를 정확히 따라야 해."

### 패턴 B: 연속된 Flow 구현 (Advanced)
의존성이 있는 일련의 Task를 묶어서 지시할 때 사용합니다.

**Prompt 예시:**
> "체크리스트 작성부터 검토까지의 흐름을 구현하고 싶어. 
> 1. @REQ-FUNC-03_Checklist_Write.md 
> 2. @REQ-FUNC-04_Review.md
> 이 두 파일을 참고해서, 먼저 작성(Write) 기능을 구현하고 이어서 검토(Review) 기능을 구현해줘."

### 패턴 C: FE PoC 일괄 생성
Epic 0의 Task들을 한 번에 참조하여 프로토타입을 빠르게 만듭니다.

**Prompt 예시:**
> "@EPIC0-FE-001_Layout_Main.md @EPIC0-FE-002_Checklist_Form.md 파일을 참고해서, 우리 서비스의 메인 레이아웃과 체크리스트 작성 폼 UI 프로토타입을 만들어줘. 디자인보다는 구조와 데이터 흐름에 집중해줘."

## 4. WBS/DAG 활용 방법
`01_Integrated_WBS_DAG.md` 파일을 통해 현재 진행 상황을 파악합니다.
- **진행 전**: DAG를 보고 선행 Task가 완료되었는지 확인합니다.
- **진행 중**: 현재 Task가 병렬 실행 가능한지(`parallelizable: true`) 확인하여 여러 에이전트(세션)를 동시에 띄울지 결정합니다.
- **완료 후**: `postconditions`를 기준으로 완료 여부를 점검합니다.

## 5. 주의사항
- **컨텍스트 유지**: Task 파일 내용이 에이전트의 컨텍스트 윈도우에 잘 들어갔는지 확인하세요.
- **파일 생성 위치**: 에이전트가 코드를 생성할 때 프로젝트의 기존 구조를 따르도록, `@project_layout` 등을 함께 참조시키는 것이 좋습니다.
- **YAML 준수**: 에이전트가 가끔 YAML의 제약조건(예: 필수 필드 검증)을 놓칠 수 있으니, 리뷰 시 `outputs.success` 기준을 만족하는지 확인하세요.


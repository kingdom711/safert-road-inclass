# 🤖 안전 지능 시스템 (GEMS) AI API 명세서

## 개요

안전의 길 프로젝트의 **안전 지능 시스템(GEMS - Generative Emergency Management System)**은 현장의 위험 상황을 AI가 분석하고 표준 조치 방안을 제시하는 핵심 기능입니다.

이 문서는 백엔드 API 개발을 위한 요청/응답 데이터 구조 및 Mock 데이터를 정의합니다.

---

## 📌 API 엔드포인트

### 위험 분석 요청

```
POST /api/gems/analyze-risk
```

---

## 📥 요청 (Request)

### Headers

```json
{
  "Content-Type": "application/json",
  "Authorization": "Bearer {access_token}"
}
```

### Request Body

```json
{
  "inputType": "TEXT",
  "inputText": "건설 현장 2층 비계 작업 중 안전난간이 심하게 흔들리고 있습니다. 작업자 3명이 해당 구역에서 철골 용접 작업을 진행 중이며, 안전대 체결 상태가 불량하여 추락 사고 위험이 매우 높은 상황입니다.",
  "photoId": null,
  "context": {
    "workType": "construction",
    "location": "2층 비계",
    "workerCount": 3,
    "currentTask": "철골 용접 작업"
  }
}
```

### Request Body 필드 설명

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `inputType` | string | ✅ | 입력 유형 (`TEXT`, `PHOTO`, `BOTH`) |
| `inputText` | string | ✅ | 위험 상황 설명 텍스트 |
| `photoId` | string | ❌ | 업로드된 사진 ID (사진 분석 시) |
| `context` | object | ❌ | 현장 컨텍스트 정보 |
| `context.workType` | string | ❌ | 작업 유형 (construction, manufacturing, etc.) |
| `context.location` | string | ❌ | 작업 위치 |
| `context.workerCount` | number | ❌ | 작업자 수 |
| `context.currentTask` | string | ❌ | 현재 수행 중인 작업 |

---

## 📤 응답 (Response)

### 성공 응답 (200 OK)

```json
{
  "success": true,
  "riskFactor": "고소 작업 중 안전대 미체결",
  "remediationSteps": [
    "즉시 작업을 중단하고 안전한 장소로 이동하십시오.",
    "안전대 및 부속품의 상태를 점검하십시오.",
    "안전대 체결 후 2인 1조로 작업을 재개하십시오."
  ],
  "referenceCode": "KOSHA-G-2023-01",
  "actionRecordId": "550e8400-e29b-41d4-a716-446655440000",
  "riskLevel": "HIGH",
  "analysisId": "analysis-2024-12-17-001",
  "analyzedAt": "2024-12-17T10:30:00.000Z"
}
```

### 응답 필드 설명

| 필드 | 타입 | 설명 |
|------|------|------|
| `success` | boolean | 분석 성공 여부 |
| `riskFactor` | string | 식별된 위험 요인 |
| `remediationSteps` | string[] | AI가 제시하는 표준 조치 방안 목록 |
| `referenceCode` | string | 관련 안전 기준 코드 (KOSHA 등) |
| `actionRecordId` | string | 생성된 조치 기록 ID |
| `riskLevel` | string | 위험 수준 (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`) |
| `analysisId` | string | 분석 고유 ID |
| `analyzedAt` | string | 분석 완료 시각 (ISO 8601) |

### 에러 응답 (4xx/5xx)

```json
{
  "success": false,
  "error": {
    "code": "ANALYSIS_FAILED",
    "message": "AI 분석 중 오류가 발생했습니다. 다시 시도해주세요.",
    "details": null
  }
}
```

---

## 🗃️ Mock 데이터 (백엔드 개발용)

### Mock 응답 데이터 세트

```javascript
const MOCK_RESPONSES = [
    {
        riskFactor: "고소 작업 중 안전대 미체결",
        remediationSteps: [
            "즉시 작업을 중단하고 안전한 장소로 이동하십시오.",
            "안전대 및 부속품의 상태를 점검하십시오.",
            "안전대 체결 후 2인 1조로 작업을 재개하십시오."
        ],
        referenceCode: "KOSHA-G-2023-01",
        riskLevel: "HIGH"
    },
    {
        riskFactor: "가연성 물질 주변 화기 작업",
        remediationSteps: [
            "반경 10m 이내 가연성 물질을 제거하거나 방염포로 덮으십시오.",
            "소화기를 작업 장소 바로 옆에 비치하십시오.",
            "화기 감시자를 배치하고 작업을 진행하십시오."
        ],
        referenceCode: "KOSHA-M-2023-05",
        riskLevel: "CRITICAL"
    },
    {
        riskFactor: "개인보호구(안전모) 미착용",
        remediationSteps: [
            "작업자에게 즉시 안전모 착용을 지시하십시오.",
            "안전모의 턱끈 체결 상태를 확인하십시오.",
            "개인보호구 착용 교육을 실시하십시오."
        ],
        referenceCode: "KOSHA-P-2023-12",
        riskLevel: "MEDIUM"
    },
    {
        riskFactor: "비계 안전난간 불량",
        remediationSteps: [
            "해당 구역 작업을 즉시 중단하십시오.",
            "안전난간 고정 상태를 점검하고 보수하십시오.",
            "비계 구조물 전체 안전 점검을 실시하십시오.",
            "작업 재개 전 관리감독자의 확인을 받으십시오."
        ],
        referenceCode: "KOSHA-C-2023-08",
        riskLevel: "HIGH"
    },
    {
        riskFactor: "밀폐공간 산소 농도 미확인",
        remediationSteps: [
            "밀폐공간 진입을 즉시 금지하십시오.",
            "산소 농도 측정기로 농도를 확인하십시오 (18% 이상 필요).",
            "환기 장치를 가동하고 충분히 환기하십시오.",
            "밀폐공간 작업 허가서를 발급받은 후 진입하십시오."
        ],
        referenceCode: "KOSHA-S-2023-03",
        riskLevel: "CRITICAL"
    },
    {
        riskFactor: "전기 작업 중 정전 미조치",
        remediationSteps: [
            "작업 전 반드시 전원을 차단하십시오.",
            "잠금장치(LOTO)를 설치하십시오.",
            "검전기로 무전압 상태를 확인하십시오.",
            "전기 작업 안전수칙을 게시하십시오."
        ],
        referenceCode: "KOSHA-E-2023-07",
        riskLevel: "CRITICAL"
    },
    {
        riskFactor: "중량물 인양 작업 구역 미통제",
        remediationSteps: [
            "인양 작업 구역에 통제선을 설치하십시오.",
            "신호수를 배치하여 작업을 통제하십시오.",
            "인양 경로 하부 출입을 금지하십시오.",
            "와이어 로프 상태를 점검하십시오."
        ],
        referenceCode: "KOSHA-L-2023-11",
        riskLevel: "HIGH"
    },
    {
        riskFactor: "용접 작업 중 화재 감시자 미배치",
        remediationSteps: [
            "화재 감시자를 즉시 배치하십시오.",
            "소화기 및 방화포를 작업 구역에 비치하십시오.",
            "작업 종료 후 30분간 화재 감시를 실시하십시오.",
            "인화성 물질을 작업 구역에서 제거하십시오."
        ],
        referenceCode: "KOSHA-F-2023-04",
        riskLevel: "HIGH"
    }
];
```

---

## 📊 데이터베이스 스키마

### gems_analysis_logs 테이블

```sql
CREATE TABLE gems_analysis_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    input_type VARCHAR(20) NOT NULL,  -- TEXT, PHOTO, BOTH
    input_text TEXT NOT NULL,
    photo_id VARCHAR(36),
    context_data JSON,
    risk_factor VARCHAR(500),
    remediation_steps JSON,
    reference_code VARCHAR(50),
    risk_level VARCHAR(20),  -- LOW, MEDIUM, HIGH, CRITICAL
    status VARCHAR(20) DEFAULT 'COMPLETED',  -- PENDING, PROCESSING, COMPLETED, FAILED
    analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### action_records 테이블

```sql
CREATE TABLE action_records (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id),
    analysis_log_id VARCHAR(36) REFERENCES gems_analysis_logs(id),
    risk_factor VARCHAR(500) NOT NULL,
    remediation_draft TEXT,
    is_ai_assisted BOOLEAN DEFAULT TRUE,
    ai_reference_code VARCHAR(50),
    status VARCHAR(20) DEFAULT 'DRAFT',  -- DRAFT, IN_PROGRESS, COMPLETED, CANCELLED
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🔄 프론트엔드 연동 코드

### 현재 geminiService.js (Mock 버전)

```javascript
/**
 * GEMS-Safety Advisor AI 시뮬레이션 서비스
 * 실제 Google Gemini API 연동 전, 프론트엔드 MVP를 위한 모의 서비스입니다.
 */

import { gemsAnalysisLogs, actionRecords } from './storage';

// 모의 응답 데이터
const MOCK_RESPONSES = [
    {
        riskFactor: '고소 작업 중 안전대 미체결',
        remediation: [
            '즉시 작업을 중단하고 안전한 장소로 이동하십시오.',
            '안전대 및 부속품의 상태를 점검하십시오.',
            '안전대 체결 후 2인 1조로 작업을 재개하십시오.'
        ],
        referenceCode: 'KOSHA-G-2023-01'
    },
    {
        riskFactor: '가연성 물질 주변 화기 작업',
        remediation: [
            '반경 10m 이내 가연성 물질을 제거하거나 방염포로 덮으십시오.',
            '소화기를 작업 장소 바로 옆에 비치하십시오.',
            '화기 감시자를 배치하고 작업을 진행하십시오.'
        ],
        referenceCode: 'KOSHA-M-2023-05'
    },
    {
        riskFactor: '개인보호구(안전모) 미착용',
        remediation: [
            '작업자에게 즉시 안전모 착용을 지시하십시오.',
            '안전모의 턱끈 체결 상태를 확인하십시오.',
            '개인보호구 착용 교육을 실시하십시오.'
        ],
        referenceCode: 'KOSHA-P-2023-12'
    }
];

export const geminiService = {
    /**
     * 위험 요인 분석 요청 (Simulated)
     * @param {string} text - 위험 상황 설명
     * @param {string} photoId - (Optional) 사진 ID
     * @param {object} context - 현장 컨텍스트 (작업유형 등)
     * @returns {Promise<object>} 분석 결과
     */
    analyzeRisk: async (text, photoId = null, context = {}) => {
        // 1. 분석 시작 로그 기록
        const log = gemsAnalysisLogs.add({
            inputType: photoId ? 'PHOTO' : 'TEXT',
            inputData: text,
            status: 'PENDING'
        });

        // 2. AI 처리 지연 시뮬레이션 (3~5초)
        const delay = Math.floor(Math.random() * 2000) + 3000;

        return new Promise((resolve) => {
            setTimeout(() => {
                // 3. 랜덤 응답 선택 (또는 입력 텍스트 기반 간단 매칭)
                const mockResponse = MOCK_RESPONSES[Math.floor(Math.random() * MOCK_RESPONSES.length)];

                // 4. 조치 기록 초안 생성
                const actionRecord = actionRecords.add({
                    riskFactor: mockResponse.riskFactor,
                    remediationDraft: mockResponse.remediation.join('\n'),
                    isAiAssisted: true,
                    aiReferenceCode: mockResponse.referenceCode,
                    originalLogId: log.id,
                    status: 'draft'
                });

                // 5. 로그 업데이트 (성공)
                // 실제 구현에서는 update 메서드가 필요하지만 MVP에서는 생략하거나 추가 구현

                resolve({
                    success: true,
                    riskFactor: mockResponse.riskFactor,
                    remediationSteps: mockResponse.remediation,
                    referenceCode: mockResponse.referenceCode,
                    actionRecordId: actionRecord.id
                });
            }, delay);
        });
    }
};
```

### 백엔드 연동 시 geminiService.js (API 호출 버전)

```javascript
/**
 * GEMS-Safety Advisor AI 서비스
 * 백엔드 API 연동 버전
 */

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

export const geminiService = {
    /**
     * 위험 요인 분석 요청
     * @param {string} text - 위험 상황 설명
     * @param {string} photoId - (Optional) 사진 ID
     * @param {object} context - 현장 컨텍스트 (작업유형 등)
     * @returns {Promise<object>} 분석 결과
     */
    analyzeRisk: async (text, photoId = null, context = {}) => {
        try {
            const response = await fetch(`${API_BASE_URL}/api/gems/analyze-risk`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
                },
                body: JSON.stringify({
                    inputType: photoId ? 'PHOTO' : 'TEXT',
                    inputText: text,
                    photoId: photoId,
                    context: context
                })
            });

            if (!response.ok) {
                throw new Error('API request failed');
            }

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('GEMS Analysis Error:', error);
            throw error;
        }
    }
};
```

---

## 🧪 테스트용 기본 입력 데이터

### 디버깅용 기본 텍스트 (DEFAULT_RISK_TEXT)

```javascript
const DEFAULT_RISK_TEXT = '건설 현장 2층 비계 작업 중 안전난간이 심하게 흔들리고 있습니다. 작업자 3명이 해당 구역에서 철골 용접 작업을 진행 중이며, 안전대 체결 상태가 불량하여 추락 사고 위험이 매우 높은 상황입니다.';
```

### 추가 테스트 시나리오

```javascript
const TEST_SCENARIOS = [
    {
        name: "고소 작업 위험",
        inputText: "건설 현장 2층 비계 작업 중 안전난간이 심하게 흔들리고 있습니다. 작업자 3명이 해당 구역에서 철골 용접 작업을 진행 중이며, 안전대 체결 상태가 불량하여 추락 사고 위험이 매우 높은 상황입니다.",
        expectedRiskLevel: "HIGH"
    },
    {
        name: "화재 위험",
        inputText: "용접 작업 구역 주변에 페인트 통과 시너 등 가연성 물질이 방치되어 있습니다. 화재 감시자가 배치되어 있지 않고, 소화기도 멀리 떨어진 곳에 있습니다.",
        expectedRiskLevel: "CRITICAL"
    },
    {
        name: "개인보호구 미착용",
        inputText: "현장에 안전모를 착용하지 않은 작업자가 발견되었습니다. 턱끈이 풀린 상태로 작업 중인 인원도 2명 확인됩니다.",
        expectedRiskLevel: "MEDIUM"
    },
    {
        name: "밀폐공간 위험",
        inputText: "지하 맨홀 작업을 시작하려고 합니다. 산소 농도 측정 없이 작업자가 진입하려 하고 있으며, 환기 장치가 준비되어 있지 않습니다.",
        expectedRiskLevel: "CRITICAL"
    },
    {
        name: "전기 작업 위험",
        inputText: "배전반 점검 작업 중 전원 차단 없이 작업이 진행되고 있습니다. LOTO 장치가 설치되어 있지 않습니다.",
        expectedRiskLevel: "CRITICAL"
    },
    {
        name: "중량물 인양 위험",
        inputText: "크레인으로 철골을 인양하는 작업 중입니다. 인양 경로 하부에 작업자들이 통제 없이 이동하고 있으며, 신호수가 배치되어 있지 않습니다.",
        expectedRiskLevel: "HIGH"
    }
];
```

---

## 📋 KOSHA 참조 코드 목록

| 코드 | 분류 | 설명 |
|------|------|------|
| `KOSHA-G-2023-01` | 고소작업 | 안전대 관련 기준 |
| `KOSHA-M-2023-05` | 화기작업 | 화재 예방 기준 |
| `KOSHA-P-2023-12` | 보호구 | 개인보호구 착용 기준 |
| `KOSHA-C-2023-08` | 가설구조 | 비계 및 거푸집 기준 |
| `KOSHA-S-2023-03` | 밀폐공간 | 밀폐공간 작업 기준 |
| `KOSHA-E-2023-07` | 전기작업 | 전기 안전 기준 |
| `KOSHA-L-2023-11` | 양중작업 | 크레인 및 양중기 기준 |
| `KOSHA-F-2023-04` | 화재예방 | 용접 화재 감시 기준 |

---

## 🔧 Spring Boot 백엔드 구현 예시

### Controller

```java
@RestController
@RequestMapping("/api/gems")
@RequiredArgsConstructor
public class GemsController {
    
    private final GemsAnalysisService gemsAnalysisService;
    
    @PostMapping("/analyze-risk")
    public ResponseEntity<GemsAnalysisResponse> analyzeRisk(
            @RequestBody GemsAnalysisRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        GemsAnalysisResponse response = gemsAnalysisService.analyzeRisk(
            request, 
            userDetails.getUsername()
        );
        
        return ResponseEntity.ok(response);
    }
}
```

### Request DTO

```java
@Data
public class GemsAnalysisRequest {
    @NotBlank
    private String inputType;  // TEXT, PHOTO, BOTH
    
    @NotBlank
    private String inputText;
    
    private String photoId;
    
    private Map<String, Object> context;
}
```

### Response DTO

```java
@Data
@Builder
public class GemsAnalysisResponse {
    private boolean success;
    private String riskFactor;
    private List<String> remediationSteps;
    private String referenceCode;
    private String actionRecordId;
    private String riskLevel;
    private String analysisId;
    private LocalDateTime analyzedAt;
}
```

---

*문서 작성일: 2024-12-17*
*버전: 1.0.0*


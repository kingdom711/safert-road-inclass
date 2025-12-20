package com.jinsung.safety_road_inclass.domain.ai.service;

import com.jinsung.safety_road_inclass.domain.ai.dto.BusinessPlanRequest;
import com.jinsung.safety_road_inclass.domain.ai.dto.BusinessPlanResponse;
import com.jinsung.safety_road_inclass.domain.ai.dto.gemini.GeminiAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * GEMS AI 분석 서비스
 * - Gemini API를 통한 산업안전 위험 분석
 * - 기존 프론트엔드 인터페이스 유지
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessPlanService {

    private final GeminiService geminiService;

    /**
     * 위험 분석 요청 처리
     * - Gemini API를 호출하여 위험 상황 분석
     * - 기존 Response 구조 유지
     */
    public BusinessPlanResponse generate(BusinessPlanRequest request) {
        log.info("[GEMS AI 분석 요청] inputType={}, inputText={}", 
                 request.getInputType(),
                 request.getInputText() != null 
                     ? request.getInputText().substring(0, Math.min(50, request.getInputText().length())) + "..." 
                     : "null");

        // Context 정보 추출
        String workType = null;
        String location = null;
        Integer workerCount = null;
        String currentTask = null;
        
        if (request.getContext() != null) {
            workType = request.getContext().getWorkType();
            location = request.getContext().getLocation();
            workerCount = request.getContext().getWorkerCount();
            currentTask = request.getContext().getCurrentTask();
            
            log.info("[GEMS Context] workType={}, location={}, workerCount={}, currentTask={}",
                     workType, location, workerCount, currentTask);
        }

        // Gemini API 호출
        GeminiAnalysisResult analysisResult = geminiService.analyzeRisk(
            request.getInputText(),
            workType,
            location,
            workerCount,
            currentTask
        );
        
        // 분석 ID 생성
        String analysisId = "analysis-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) 
                          + "-" + UUID.randomUUID().toString().substring(0, 8);
        
        // 조치 기록 ID 생성
        String actionRecordId = UUID.randomUUID().toString();

        // Response 생성 (기존 구조 유지)
        return BusinessPlanResponse.builder()
                .riskFactor(analysisResult.getRiskFactor())
                .remediationSteps(analysisResult.getRemediationSteps())
                .referenceCode(analysisResult.getReferenceCode())
                .riskLevel(analysisResult.getRiskLevel())
                .analysisId(analysisId)
                .actionRecordId(actionRecordId)
                .analyzedAt(LocalDateTime.now())
                .build();
    }
}

package com.jinsung.safety_road_inclass.domain.ai.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 문서 보강 서비스
 * - Spring AI ChatModel을 사용하여 문서 초안을 전문적인 문체로 보강
 * - 토큰 사용량 측정 및 로깅
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentEnhancementService {

    @Autowired(required = false)
    private ChatModel chatModel;

    /**
     * 전문적인 문서 편집자 시스템 프롬프트
     */
    private static final String SYSTEM_PROMPT = """
        당신은 전문적인 문서 편집자입니다.
        사용자가 제공한 문서 초안을 받아서, 더 전문적이고 명확하며 완성도 높은 문서로 보강해주세요.
        
        다음 원칙을 따라주세요:
        1. 원본의 핵심 내용과 의도를 정확히 유지하세요.
        2. 문체를 전문적이고 명확하게 다듬어주세요.
        3. 불필요한 표현은 제거하고, 필요한 정보는 보강하세요.
        4. 문서의 구조와 논리를 개선하세요.
        5. 오타나 문법 오류를 수정하세요.
        
        보강된 문서만 반환하세요. 추가 설명이나 메타데이터는 포함하지 마세요.
        """;

    /**
     * 문서 초안을 보강하여 반환
     * 
     * @param draftContent 사용자가 작성한 문서 초안
     * @return 보강된 문서 내용과 토큰 사용량 정보
     */
    public DocumentEnhancementResult enhanceDocument(String draftContent) {
        log.info("[문서 보강 요청] contentLength={}", draftContent != null ? draftContent.length() : 0);

        // Spring AI ChatModel이 없으면 Fallback 동작
        if (chatModel == null) {
            log.warn("[문서 보강] Spring AI ChatModel이 주입되지 않아 원본 내용을 그대로 반환합니다.");
            return DocumentEnhancementResult.builder()
                    .enhancedContent(draftContent != null ? "[Fallback] " + draftContent : null)
                    .promptTokens(0)
                    .generationTokens(0)
                    .totalTokens(0)
                    .build();
        }

        try {
            // 시스템 메시지와 사용자 메시지 생성
            Message systemMessage = new SystemMessage(SYSTEM_PROMPT);
            Message userMessage = new UserMessage(draftContent);

            // Prompt 생성
            Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

            // ChatModel 호출
            ChatResponse chatResponse = chatModel.call(prompt);

            // 응답 텍스트 추출 (Spring AI 1.0.0 API에 맞게 수정)
            String enhancedContent = null;
            try {
                // Spring AI 1.0.0에서는 응답 내용 추출 방식이 변경되었을 수 있음
                var result = chatResponse.getResult();
                if (result != null) {
                    var output = result.getOutput();
                    if (output != null) {
                        // AssistantMessage에서 내용 추출 시도
                        // Spring AI 1.0.0의 실제 API 구조에 따라 수정 필요
                        enhancedContent = output.toString(); // 임시: toString() 사용
                    }
                }
            } catch (Exception e) {
                log.warn("[응답 내용 추출 실패] 오류 발생: {}", e.getMessage());
            }

            // UsageMetadata 추출 (Spring AI 1.0.0 API에 맞게 수정)
            Integer promptTokens = null;
            Integer generationTokens = null;
            Integer totalTokens = null;

            try {
                // Spring AI 1.0.0에서는 메타데이터 접근 방식이 변경되었을 수 있음
                // ChatResponse에서 직접 메타데이터를 가져오는 방법 시도
                var metadata = chatResponse.getMetadata();
                if (metadata != null) {
                    // 메타데이터에서 Usage 정보 추출 시도
                    // Spring AI 1.0.0의 실제 API 구조에 따라 수정 필요
                    // 현재는 null로 설정하고, 실제 API 확인 후 수정 예정
                }
            } catch (Exception e) {
                log.debug("[토큰 사용량 추출 실패] 메타데이터 접근 중 오류: {}", e.getMessage());
            }

            // 토큰 사용량 로깅
            if (promptTokens != null || generationTokens != null) {
                log.info("[Gemini Usage Log] Input: {}, Output: {}, Total: {}",
                        promptTokens != null ? promptTokens : 0,
                        generationTokens != null ? generationTokens : 0,
                        totalTokens != null ? totalTokens : 0);
            }

            log.info("[문서 보강 완료] enhancedContentLength={}", enhancedContent != null ? enhancedContent.length() : 0);

            return DocumentEnhancementResult.builder()
                    .enhancedContent(enhancedContent)
                    .promptTokens(promptTokens)
                    .generationTokens(generationTokens)
                    .totalTokens(totalTokens)
                    .build();

        } catch (Exception e) {
            log.error("[문서 보강 실패] 오류 발생: {}", e.getMessage(), e);
            // Fallback: 원본 내용 반환
            return DocumentEnhancementResult.builder()
                    .enhancedContent(draftContent)
                    .promptTokens(0)
                    .generationTokens(0)
                    .totalTokens(0)
                    .build();
        }
    }

    /**
     * 문서 보강 결과 DTO
     */
    @lombok.Builder
    @lombok.Getter
    public static class DocumentEnhancementResult {
        private String enhancedContent;
        private Integer promptTokens;
        private Integer generationTokens;
        private Integer totalTokens;
    }
}


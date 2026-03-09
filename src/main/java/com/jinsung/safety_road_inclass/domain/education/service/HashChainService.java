package com.jinsung.safety_road_inclass.domain.education.service;

import com.jinsung.safety_road_inclass.domain.education.entity.EducationCompletion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.UUID;

/**
 * HashChainService - SHA-256 해시 체이닝 서비스
 *
 * 목적: 교육 수료 레코드의 사후 위변조를 기술적으로 탐지
 *
 * 원리:
 *   record_n.hash = SHA256(record_n의 핵심 필드 | record_{n-1}.hash)
 *   → 중간 레코드 수정 시 이후 모든 해시가 무효화됨
 */
@Service
@Slf4j
public class HashChainService {

    private static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    /**
     * 교육 수료 레코드의 무결성 해시 생성
     *
     * @param record  완성된 EducationCompletion 엔티티 (id 포함)
     * @param prevHash 이전 레코드의 integrityHash (최초면 null)
     * @return SHA-256 해시 문자열 (hex, 64자)
     */
    public String generateHash(EducationCompletion record, String prevHash) {
        String chain = (prevHash != null && !prevHash.isBlank()) ? prevHash : GENESIS_HASH;

        String raw = String.format("%d|%d|%s|%s|%s|%d|%.4f|%d|%s",
                record.getUser().getId(),
                record.getId(),
                record.getEducationId(),
                record.getCompletedAt().toString(),
                record.getEducationType().name(),
                record.getQuizScore(),
                record.getVideoWatchRatio(),
                record.getAttemptCount(),
                chain
        );

        return sha256Hex(raw);
    }

    /**
     * 해시 체인 검증
     *
     * @param record   검증할 레코드
     * @param prevHash 이전 레코드의 저장된 해시
     * @return true = 무결성 정상, false = 위변조 의심
     */
    public boolean verify(EducationCompletion record, String prevHash) {
        String expected = generateHash(record, prevHash);
        boolean valid = expected.equals(record.getIntegrityHash());

        if (!valid) {
            log.warn("[HashChain] 무결성 검증 실패! completionId={}, expected={}, actual={}",
                    record.getId(), expected, record.getIntegrityHash());
        }

        return valid;
    }

    /**
     * 수료증 번호 생성 (CERT-YYYYMM-UUID8)
     * 예: CERT-202503-A1B2C3D4
     */
    public String generateCertNumber() {
        String yearMonth = YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String uuid8 = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return String.format("CERT-%s-%s", yearMonth, uuid8);
    }

    // ─── 내부 유틸 ───────────────────────────────────────────────────

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256은 Java 표준 → 실질적으로 발생하지 않음
            throw new IllegalStateException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
        }
    }
}

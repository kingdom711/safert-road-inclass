package com.jinsung.safety_road_inclass.domain.education.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * WatchCheckpointResponse - 체크포인트 저장 결과
 */
@Getter
@Builder
public class WatchCheckpointResponse {

    private Long logId;          // 저장된 로그 ID
    private String serverTime;   // 서버 기록 시각 (ISO-8601)
    private int checkpointSec;   // 저장된 재생 위치
}

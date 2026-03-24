package com.jinsung.safety_road_inclass.domain.workstop.dto;

import com.jinsung.safety_road_inclass.domain.workstop.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WorkStopStatusUpdateRequest {

    @NotNull(message = "변경할 상태는 필수입니다.")
    private ReportStatus status;

    private String note;

    /** RESOLVED 상태일 때 조치 정보 */
    private String actionPlan;
    private String actionMethod;

    /** RESUMED 상태일 때 재개 확인 정보 */
    private String resumeVerification;
}

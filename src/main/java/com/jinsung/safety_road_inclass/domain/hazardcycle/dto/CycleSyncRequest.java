package com.jinsung.safety_road_inclass.domain.hazardcycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "오프라인 사이클 동기화 요청 항목")
public class CycleSyncRequest {

    @NotBlank(message = "clientTempId는 필수입니다.")
    @Size(max = 100, message = "clientTempId는 최대 100자입니다.")
    private String clientTempId;

    @Size(max = 2000, message = "description은 최대 2000자입니다.")
    private String description;

    @Size(max = 500, message = "location은 최대 500자입니다.")
    private String location;

    private LocalDateTime reportedAt;

    @NotBlank(message = "photoFileName은 필수입니다.")
    @Size(max = 255, message = "photoFileName은 최대 255자입니다.")
    private String photoFileName;
}

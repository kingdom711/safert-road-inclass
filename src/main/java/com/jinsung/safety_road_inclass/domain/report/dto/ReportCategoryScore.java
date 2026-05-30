package com.jinsung.safety_road_inclass.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportCategoryScore {

    private String key;
    private String label;
    private int score;
    private String detail;
}

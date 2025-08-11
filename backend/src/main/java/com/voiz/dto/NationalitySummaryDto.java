package com.voiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NationalitySummaryDto {
    private long localCount;                 // 내국인 수 (예: KR)
    private long foreignCount;               // 외국인 수 (전체 - 내국인)
    private List<NationalityAnalyticsDto> breakdown; // 국적별 분포
}



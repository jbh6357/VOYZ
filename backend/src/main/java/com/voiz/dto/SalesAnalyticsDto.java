package com.voiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesAnalyticsDto {
    
    // 선택한 시간 그룹 
    private String granurality; 
    
    // 해당 시간 그룹의 총 매출
    private Double  totalSales;

    
}
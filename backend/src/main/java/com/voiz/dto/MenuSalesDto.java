package com.voiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuSalesDto {
    private String menuName;
    private Double salesAmount;     // 실제 매출액
    private Double salesPercentage; // 비율 (필요시 사용)
    
    // 매출액만 사용하는 생성자
    public MenuSalesDto(String menuName, Double salesAmount) {
        this.menuName = menuName;
        this.salesAmount = salesAmount;
        this.salesPercentage = null;
    }
}
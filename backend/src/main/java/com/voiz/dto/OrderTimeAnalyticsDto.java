package com.voiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTimeAnalyticsDto {
    private String hour;      
    private Long orderCount;  
}
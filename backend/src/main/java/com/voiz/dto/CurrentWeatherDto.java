package com.voiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentWeatherDto {
    private Double temp; // 현재 기온
    private Integer sky;  // 하늘 상태 (추가정보 : 1:맑음, 3:구름많음, 4:흐림)
    private Integer pty;  // 강수 형태 (추가정보 : 0:없음, 1:비, 2:비/눈, 3:눈, 4:소나기)
    private Integer reh;  // 습도 (%)
    
}
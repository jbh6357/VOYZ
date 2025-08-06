package com.voiz.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDto {
    private LocalDate forecastDate; // DB 컬럼명과 통일
    private Double tmx;             // 일 최고기온 
    private Double tmn;             // 일 최저기온 
    private Integer reh;            // 습도 
    private Integer pop;            // 강수확률 
    private Integer sky;            // 하늘상태
    private Integer pty;            // 강수형태

    // tmx, tmn이 null일 경우에는 temp에서 최대, 최소값을 서비스단에서 받아옴
}
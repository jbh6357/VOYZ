package com.voiz.vo;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "VOYZ_WEATHER")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "weather_seq_generator")
    @SequenceGenerator(name = "weather_seq_generator", sequenceName = "VOYZ_WEATHER_SEQ", allocationSize = 1)
    @Column(name = "WEATHER_IDX")
    private Long weatherIdx;

    @Column(name = "DONG_NAME")
    private String dongName; // 동네 이름(동 기준)

    @Column(name = "FORECAST_DATE")
    private LocalDate forecastDate; // 일기 예보 날짜

    @Column(name = "HOUR")
    private String hour; // 일기예보 시간 

    @Column(name = "TEMP")
    private Double temp; // 기온

    @Column(name = "TMX")
    private Double tmx; // 일 최고기온

    @Column(name = "TMN")
    private Double tmn; // 일 최저기온

    @Column(name = "SKY")
    private Integer sky; // 하늘상태

    @Column(name = "PTY")
    private Integer pty; // 강수형태

    @Column(name = "POP")
    private Integer pop; // 강수확률

    @Column(name = "REH")
    private Integer reh; // 습도

    @Column(name = "WS")
    private Double ws; // 풍속
}
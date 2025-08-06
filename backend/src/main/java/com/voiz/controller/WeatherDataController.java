// backend/src/main/java/com/voiz/controller/WeatherDataController.java

package com.voiz.controller;

import java.time.LocalDate;

// import java.time.LocalDate; // 더 이상 필요 없으므로 삭제 가능
import org.springframework.beans.factory.annotation.Autowired; // Autowired 추가
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.voiz.dto.CurrentWeatherDto;
import com.voiz.dto.ForecastResponseDto;
import com.voiz.dto.WeatherDto;
import com.voiz.dto.WeatherResponseDto;
import com.voiz.service.WeatherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/weather")
@Tag(name = "data-collect", description = "데이터 수집 API")
public class WeatherDataController {

    @Autowired // WeatherService를 스프링 컨테이너가 자동으로 주입.
    private WeatherService weatherService;

    @PostMapping("/weather-data")
    @Operation(summary = "날씨 데이터 수집", description = "기상청 API를 이용하여 특정 동네의 날씨 예보 데이터를 수집하고 DB에 저장합니다.")
    
    public ResponseEntity<Void> collectWeather(@RequestParam String address) {
        // 주소를 GeoConverter를 통해 위경도와 '동' 이름으로 변환.
        // WeatherService의 weatherData 메서드를 호출하여 데이터를 수집하고 저장.
        boolean success = weatherService.weatherData(address);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/current")
    public ResponseEntity<ForecastResponseDto<CurrentWeatherDto>> getCurrentWeather(@RequestParam("user_id") String userId) {
        ForecastResponseDto<CurrentWeatherDto> weatherResponse = weatherService.getCurrentWeather(userId);
        return ResponseEntity.ok(weatherResponse);
    }



    @GetMapping("/forecast")
    @Operation(summary = "일일 예보 정보 조회", description = "사용자의 가게 위치와 특정 날짜(yyyy-MM-dd)를 기준으로 일일 예보를 조회합니다.")
    public ResponseEntity<ForecastResponseDto<WeatherDto>> getDailyForecast(
            @RequestParam("user_id") String userId,
            @RequestParam("date") String dateString) {

        LocalDate date = LocalDate.parse(dateString); // "yyyy-MM-dd" 형식의 문자열을 날짜 객체로 변환
        ForecastResponseDto<WeatherDto> dailyForecast = weatherService.getDailyForecast(userId, date);
        return ResponseEntity.ok(dailyForecast);
    }

}
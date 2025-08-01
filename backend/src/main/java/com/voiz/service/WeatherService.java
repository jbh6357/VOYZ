package com.voiz.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiz.dto.CurrentWeatherDto;
import com.voiz.dto.ForecastResponseDto;
import com.voiz.dto.WeatherDto;
import com.voiz.dto.WeatherResponseDto;
import com.voiz.mapper.WeatherRepository;
import com.voiz.util.GeoConverter; // 연결될 유틸리티
import com.voiz.util.WeatherApi;   // 연결될 유틸리티
import com.voiz.vo.Users;
import com.voiz.vo.Weather;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.voiz.dto.CurrentWeatherDto; 
import com.voiz.mapper.UsersRepository; 

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WeatherService {

    
    @Autowired private WeatherRepository weatherRepository; // DB 
    @Autowired private WeatherApi weatherApi;               // 기상청 API 기능
    @Autowired private GeoConverter geoConverter;           // 좌표 변환 기능
    @Autowired private UsersRepository usersRepository;
    

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public boolean weatherData(String address) {
        try {
            // 주소를 좌표로 변환 
            GeoConverter.GeoData geoData = geoConverter.convertAddressToCoordinates(address)
                    .orElseThrow(() -> new IllegalArgumentException("주소 변환 실패: " + address));

            // 좌표를 기상청 격자로 변환 (GeoConverter가 구현)
            GeoConverter.GridData gridData = geoConverter.convertLatLonToGrid(geoData.lat(), geoData.lon());

            // 격자 좌표로 날씨 API 호출 (WeatherApi가 구현)
            String weatherJson = weatherApi.getWeatherData(String.valueOf(gridData.nx()), String.valueOf(gridData.ny()))
                    .orElseThrow(() -> new RuntimeException("날씨 정보 조회 실패."));

            // 받아온 데이터를 파싱하고 DB에 저장할 객체로 변환
            List<Weather> weatherList = parseAndPrepareData(weatherJson, geoData.dongName());

            // 최종 데이터를 DB에 저장 (WeatherRepository가 구현)
            weatherRepository.saveAll(weatherList);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 기상청에서 받은 JSON 데이터를 파싱 및 Weather 객체 리스트로 변환.
     */
    private List<Weather> parseAndPrepareData(String json, String dongName) throws Exception {
        Map<String, Map<String, Map<String, String>>> forecasts = new LinkedHashMap<>();
        JsonNode items = objectMapper.readTree(json).path("response").path("body").path("items").path("item");

        if (items.isArray()) {
            for (JsonNode item : items) {
                forecasts.computeIfAbsent(item.get("fcstDate").asText(), k -> new LinkedHashMap<>())
                         .computeIfAbsent(item.get("fcstTime").asText(), k -> new LinkedHashMap<>())
                         .put(item.get("category").asText(), item.get("fcstValue").asText());
            }
        }

        List<Weather> weatherEntities = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        for (var dateEntry : forecasts.entrySet()) {
            String date = dateEntry.getKey();
            var times = dateEntry.getValue();
            String dailyTmx = times.values().stream().filter(v -> v.containsKey("TMX")).map(v -> v.get("TMX")).findFirst().orElse(null);
            String dailyTmn = times.values().stream().filter(v -> v.containsKey("TMN")).map(v -> v.get("TMN")).findFirst().orElse(null);

            for (var timeEntry : times.entrySet()) {
                if (!timeEntry.getValue().containsKey("TMP")) continue; // 기온(TMP) 없으면 스킵

                Weather weather = new Weather();
                weather.setDongName(dongName);
                weather.setForecastDate(LocalDate.parse(date, dateFormatter));
                weather.setHour(timeEntry.getKey());
                weather.setTemp(Double.parseDouble(timeEntry.getValue().get("TMP")));
                weather.setTmx(dailyTmx != null ? Double.parseDouble(dailyTmx) : null);
                weather.setTmn(dailyTmn != null ? Double.parseDouble(dailyTmn) : null);
                weather.setSky(Integer.parseInt(timeEntry.getValue().getOrDefault("SKY", "0")));
                weather.setPty(Integer.parseInt(timeEntry.getValue().getOrDefault("PTY", "0")));
                weather.setPop(Integer.parseInt(timeEntry.getValue().getOrDefault("POP", "0")));
                weather.setReh(Integer.parseInt(timeEntry.getValue().getOrDefault("REH", "0")));
                weather.setWs(Double.parseDouble(timeEntry.getValue().getOrDefault("WSD", "0.0")));
                weatherEntities.add(weather);
            }
        }
        return weatherEntities;



        // 현재 날씨 정보 조회 
        


    }



     @Transactional(readOnly = true)
    public ForecastResponseDto<CurrentWeatherDto> getCurrentWeather(String userId) {
        Users user = usersRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        String storeAddress = user.getStoreAddress();
        if (storeAddress == null || storeAddress.isBlank()) { throw new RuntimeException("가게 주소가 등록되지 않은 사용자입니다."); }
        String dongName = geoConverter.convertAddressToCoordinates(storeAddress).map(GeoConverter.GeoData::dongName).orElseThrow(() -> new RuntimeException("주소에서 '동' 정보를 추출할 수 없습니다."));

        LocalDate today = LocalDate.now();
        List<Weather> todayWeatherList = weatherRepository.findWeatherByDongNameAndDateRange(dongName, today, today);
        if (todayWeatherList.isEmpty()) { throw new RuntimeException("오늘의 날씨 정보가 DB에 없습니다."); }

        LocalDateTime now = LocalDateTime.now();
        String currentHourStr = now.format(DateTimeFormatter.ofPattern("HH00"));
        Weather currentWeather = todayWeatherList.stream()
                .filter(w -> Integer.parseInt(w.getHour()) <= Integer.parseInt(currentHourStr))
                .max(Comparator.comparing(Weather::getHour))
                .orElseThrow(() -> new RuntimeException("현재 시간의 날씨 정보를 찾을 수 없습니다."));

        CurrentWeatherDto detailsDto = new CurrentWeatherDto(currentWeather.getTemp(), currentWeather.getSky(), currentWeather.getPty(), currentWeather.getReh());
        String description = generateCurrentWeatherDescription(todayWeatherList, detailsDto);
        return new ForecastResponseDto<>(description, detailsDto);
    }

    /**
     * [미래 예보 조회 기능] 특정 사용자의 '지정된 날짜'에 대한 일일 예보를 조회합니다.
     */
    @Transactional(readOnly = true)
    public ForecastResponseDto<WeatherDto> getDailyForecast(String userId, LocalDate date) {
        Users user = usersRepository.findByUserId(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));
        String storeAddress = user.getStoreAddress();
        if (storeAddress == null || storeAddress.isBlank()) { throw new RuntimeException("가게 주소가 등록되지 않은 사용자입니다."); }
        String dongName = geoConverter.convertAddressToCoordinates(storeAddress).map(GeoConverter.GeoData::dongName).orElseThrow(() -> new RuntimeException("주소에서 '동' 정보를 추출할 수 없습니다."));

        List<Weather> dailyWeatherList = weatherRepository.findWeatherByDongNameAndDateRange(dongName, date, date);
        if (dailyWeatherList.isEmpty()) { throw new RuntimeException(date + "의 날씨 정보가 DB에 없습니다."); }

        WeatherDto detailsDto = createDailyWeatherDto(dailyWeatherList);
        String description = generateForecastDescription(detailsDto);
        return new ForecastResponseDto<>(description, detailsDto);
    }

    // --- Private Helper Methods ---

    private String generateCurrentWeatherDescription(List<Weather> todayWeatherList, CurrentWeatherDto currentWeather) {
        DoubleSummaryStatistics stats = todayWeatherList.stream().mapToDouble(Weather::getTemp).summaryStatistics();
        String skyState;
        switch (currentWeather.getSky()) {
            case 1: skyState = "맑음"; break;
            case 3: skyState = "구름많음"; break;
            case 4: skyState = "흐림"; break;
            default: skyState = "알 수 없음"; break;
        }
        String ptyState;
        switch (currentWeather.getPty()) {
            case 1: ptyState = ", 비가 와요."; break;
            case 2: ptyState = ", 비나 눈이 와요."; break;
            case 3: ptyState = ", 눈이 와요."; break;
            case 4: ptyState = ", 소나기가 와요."; break;
            default: ptyState = "입니다."; break;
        }
        return String.format("오늘 최고기온은 %.0f도, 최저기온은 %.0f도이며, 현재 날씨는 '%s'%s", stats.getMax(), stats.getMin(), skyState, ptyState);
    }

    private WeatherDto createDailyWeatherDto(List<Weather> dailyWeather) {
        LocalDate date = dailyWeather.get(0).getForecastDate();
        Double tmx = dailyWeather.stream().map(Weather::getTmx).filter(Objects::nonNull).findFirst().orElseGet(() -> dailyWeather.stream().mapToDouble(Weather::getTemp).max().orElse(0.0));
        Double tmn = dailyWeather.stream().map(Weather::getTmn).filter(Objects::nonNull).findFirst().orElseGet(() -> dailyWeather.stream().mapToDouble(Weather::getTemp).min().orElse(0.0));
        Integer reh = (int) dailyWeather.stream().mapToInt(Weather::getReh).average().orElse(0);
        Integer pop = dailyWeather.stream().mapToInt(Weather::getPop).max().orElse(0);
        Weather representativeWeather = dailyWeather.stream().filter(w -> "1200".equals(w.getHour())).findFirst().orElse(dailyWeather.get(0));
        return new WeatherDto(date, tmx, tmn, reh, pop, representativeWeather.getSky(), representativeWeather.getPty());
    }

    private String generateForecastDescription(WeatherDto forecast) {
        String skyState;
        switch (forecast.getSky()) {
            case 1: skyState = "맑음"; break;
            case 3: skyState = "구름많음"; break;
            case 4: skyState = "흐림"; break;
            default: skyState = "알 수 없음"; break;
        }
        String ptyState = "";
        if (forecast.getPty() > 0) {
            switch (forecast.getPty()) {
                case 1: ptyState = " 비 소식"; break;
                case 2: ptyState = " 비 또는 눈 소식"; break;
                case 3: ptyState = " 눈 소식"; break;
                case 4: ptyState = " 소나기 소식"; break;
            }
        }
        return String.format("%d월 %d일: 최고 %.0f°, 최저 %.0f°, %s", forecast.getForecastDate().getMonthValue(), forecast.getForecastDate().getDayOfMonth(), forecast.getTmx(), forecast.getTmn(), skyState, ptyState);
    }

}
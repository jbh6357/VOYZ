package com.voiz.util;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class WeatherApi {

    public static final String SERVICE_KEY = "Jh+qx6lvBpNoI54Wk48m6uiCTbx/La68eVaDXDTQ+vuKqMqdo24ZhlznKur8ZKvowJ8nTcnlC6mLgQW9GfSHJA==";
    public static final String BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";
    private final RestTemplate restTemplate = new RestTemplate();

    public Optional<String> getWeatherData(String nx, String ny) {
        LocalDateTime now = LocalDateTime.now();
        String baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = getBaseTime(now);

        if (baseTime.equals("2300") && now.getHour() < 2) {
            baseDate = now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        try {
            StringBuilder urlBuilder = new StringBuilder(BASE_URL);
            // URL 인코딩을 사용하여 파라미터를 추가합니다.
            urlBuilder.append("?" + URLEncoder.encode("serviceKey", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(SERVICE_KEY, StandardCharsets.UTF_8));
            urlBuilder.append("&" + URLEncoder.encode("pageNo", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("1", StandardCharsets.UTF_8));
            urlBuilder.append("&" + URLEncoder.encode("numOfRows", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("1000", StandardCharsets.UTF_8));
            urlBuilder.append("&" + URLEncoder.encode("dataType", StandardCharsets.UTF_8) + "=" + URLEncoder.encode("JSON", StandardCharsets.UTF_8));
            urlBuilder.append("&" + URLEncoder.encode("base_date", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(baseDate, StandardCharsets.UTF_8));
            urlBuilder.append("&" + URLEncoder.encode("base_time", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(baseTime, StandardCharsets.UTF_8));
            urlBuilder.append("&" + URLEncoder.encode("nx", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(nx, StandardCharsets.UTF_8));
            urlBuilder.append("&" + URLEncoder.encode("ny", StandardCharsets.UTF_8) + "=" + URLEncoder.encode(ny, StandardCharsets.UTF_8));

            URI uri = new URI(urlBuilder.toString());
            String response = restTemplate.getForObject(uri, String.class);
            return Optional.ofNullable(response);

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private String getBaseTime(LocalDateTime now) {
        int hour = now.getHour();
        if (now.getMinute() < 45) {
            hour = now.minusHours(1).getHour();
        }
        if (hour < 2) return "2300";
        if (hour < 5) return "0200";
        if (hour < 8) return "0500";
        if (hour < 11) return "0800";
        if (hour < 14) return "1100";
        if (hour < 17) return "1400";
        if (hour < 20) return "1700";
        if (hour < 23) return "2000";
        return "2300";
    }
}
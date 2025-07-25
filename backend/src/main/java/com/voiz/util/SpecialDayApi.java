package com.voiz.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiz.vo.SpecialDay;

@Component
public class SpecialDayApi {

	// API 호출에 필요한 인증 키
    public static final String SERVICE_KEY = "Jh+qx6lvBpNoI54Wk48m6uiCTbx/La68eVaDXDTQ+vuKqMqdo24ZhlznKur8ZKvowJ8nTcnlC6mLgQW9GfSHJA==";
    
    // Base URL
    public static final String BASE_URL = "http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService";
    
    public List<SpecialDay> getSpecialDay(String year, String month, String endpoint, String type) throws IOException {
    	
    	List<SpecialDay> result = new ArrayList<>();
    	
    	StringBuilder urlBuilder = new StringBuilder(BASE_URL + "/" + endpoint); /*URL*/
    	urlBuilder.append("?" + URLEncoder.encode("ServiceKey","UTF-8") + "=" + URLEncoder.encode(SERVICE_KEY, "UTF-8")); /*Service Key*/
        urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("100", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("solYear","UTF-8") + "=" + URLEncoder.encode(year, "UTF-8")); /*연*/
        urlBuilder.append("&" + URLEncoder.encode("solMonth","UTF-8") + "=" + URLEncoder.encode(month, "UTF-8")); /*월*/
        urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("json", "UTF-8")); /*월*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
        System.out.println("🔍 요청 URL: " + urlBuilder.toString());
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        System.out.println(sb.toString());
        
     // 👇 JSON 파싱 후 리스트에 add
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(sb.toString());
        JsonNode items = root.path("response").path("body").path("items").path("item");
        
        if (items.isArray()) {
            for (JsonNode item : items) {
                SpecialDay specialDay = new SpecialDay();
                specialDay.setName(item.path("dateName").asText());
                specialDay.setType(type);
                //specialDay.setCategory("기타");
                String locdate = item.path("locdate").asText();
                LocalDate date = LocalDate.parse(locdate, DateTimeFormatter.ofPattern("yyyyMMdd"));
                specialDay.setStartDate(date);
                specialDay.setEndDate(date);
                String isHolidayStr = item.path("isHoliday").asText("N");
                specialDay.setIsHoliday("Y".equals(isHolidayStr) ? 1 : 0);
                result.add(specialDay);
            }
        }
        
        return result;

    }
  
    
    
}

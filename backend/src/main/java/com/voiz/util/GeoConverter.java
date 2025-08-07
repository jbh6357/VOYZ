// backend/src/main/java/com/voiz/util/GeoConverter.java
package com.voiz.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.Optional;

@Component
public class GeoConverter {

    private static final String KAKAO_KEY = "289d2b458baae257442cb5ac55c26946";
    private static final String KAKAO_URL = "https://dapi.kakao.com/v2/local/search/address.json";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 주소를 위경도와 '동' 이름으로 변환.
     */
    public Optional<GeoData> convertAddressToCoordinates(String address) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + KAKAO_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = KAKAO_URL + "?query=" + address;

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode doc = objectMapper.readTree(response.getBody()).path("documents").get(0);
            if (doc == null || doc.isMissingNode()) return Optional.empty();

            double lat = doc.get("y").asDouble();
            double lon = doc.get("x").asDouble();
            String sido = doc.path("address").path("region_1depth_name").asText(null);
            String gugun = doc.path("address").path("region_2depth_name").asText(null);
            String dongName = Optional.ofNullable(doc.path("address").path("region_3depth_name").asText(null))
                                    .orElse(doc.path("road_address").path("region_3depth_name").asText(null));

            return Optional.of(new GeoData(lat, lon, sido, gugun, dongName));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * 위경도를 기상청 격자 좌표로 변환(이에 특화된 특별한 함수식 사용).
     */
    public GridData convertLatLonToGrid(double lat, double lon) {
        // Python 코드의 latlon_to_xy 함수 로직을 그대로 Java로 구현
        double RE = 6371.00877, GRID = 5.0, SLAT1 = 30.0, SLAT2 = 60.0, OLON = 126.0, OLAT = 38.0;
        double XO = 43, YO = 136;
        double DEGRAD = Math.PI / 180.0;
        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD, slat2 = SLAT2 * DEGRAD, olon = OLON * DEGRAD, olat = OLAT * DEGRAD;

        double sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5));
        double sf = Math.pow(Math.tan(Math.PI * 0.25 + slat1 * 0.5), sn) * Math.cos(slat1) / sn;
        double ro = re * sf / Math.pow(Math.tan(Math.PI * 0.25 + olat * 0.5), sn);
        double ra = re * sf / Math.pow(Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5), sn);
        double theta = lon * DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        int nx = (int) (ra * Math.sin(theta) + XO + 0.5);
        int ny = (int) (ro - ra * Math.cos(theta) + YO + 0.5);
        return new GridData(nx, ny);
    }

    // 사용할 데이터 전달 record 
    public record GeoData(double lat, double lon, String sido, String gugun,  String dongName ) {}
    public record GridData(int nx, int ny) {}
}
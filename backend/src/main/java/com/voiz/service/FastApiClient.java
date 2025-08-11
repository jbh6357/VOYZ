package com.voiz.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiz.util.MultipartFileResource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import java.util.Map;
import java.io.IOException;
import java.util.HashMap;

@Service
public class FastApiClient {
    
    @Value("${fastapi.base-url:http://127.0.0.1:8000}")
    private String fastApiBaseUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * FastAPI에서 데이터를 가져오는 메서드
     * @param endpoint FastAPI 엔드포인트 (예: "/api/data")
     * @return 응답 데이터
     */
    public ResponseEntity<String> getDataFromFastApi(String endpoint) {
        String url = fastApiBaseUrl + endpoint;
        return restTemplate.getForEntity(url, String.class);
    }
    
    /**
     * FastAPI에 데이터를 전송하는 메서드
     * @param endpoint FastAPI 엔드포인트
     * @param data 전송할 데이터
     * @return 응답 데이터
     */
    public ResponseEntity<String> postDataToFastApi(String endpoint, Map<String, Object> data) {
        String url = fastApiBaseUrl + endpoint;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(data, headers);
        return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
    }
    
    /**
     * 데이터 분석 결과를 가져오는 메서드
     * @param analysisType 분석 유형
     * @param parameters 분석 파라미터
     * @return 분석 결과
     */
    public ResponseEntity<String> getAnalysisResult(String analysisType, Map<String, Object> parameters) {
        String endpoint = "/api/analysis/" + analysisType;
        return postDataToFastApi(endpoint, parameters);
    }
    
    /**
     * 예측 모델 결과를 가져오는 메서드
     * @param modelName 모델 이름
     * @param inputData 입력 데이터
     * @return 예측 결과
     */
    public ResponseEntity<String> getPrediction(String modelName, Map<String, Object> inputData) {
        String endpoint = "/api/predict/" + modelName;
        return postDataToFastApi(endpoint, inputData);
    }
    
    public ResponseEntity<String> getMatchResult(String modelName, Map<String, Object> inputData){
    	String endpoint = "/api/match/" + modelName;
        return postDataToFastApi(endpoint, inputData);
    }
    
    /**
     * 특일 컨텐츠 생성 메서드
     * @param name 특일명
     * @param type 특일 유형
     * @param category 카테고리 (선택)
     * @return 생성된 컨텐츠
     */
    public ResponseEntity<String> generateSpecialDayContent(String name, String type, String category) {
        String endpoint = "/api/content/generate";
        
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("type", type);
        data.put("category", category);
        data.put("startDate", ""); // 필요시 추가
        data.put("endDate", "");   // 필요시 추가
        
        return postDataToFastApi(endpoint, data);
    }
    
    /**
     * 특일 카테고리 분류 메서드
     * @param name 특일명
     * @param type 특일 유형
     * @param category 카테고리 (선택)
     * @return 분류된 카테고리 목록
     */
    public ResponseEntity<String> classifySpecialDayCategories(String name, String type, String category) {
        String endpoint = "/api/category/classify";
        
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("type", type);
        data.put("category", category);
        
        return postDataToFastApi(endpoint, data);
    }

    /**
     * 
     * @param name
     * @param type
     * @param storeCategory
     * @return
     */
	public ResponseEntity<String> createSpecialDaySugForUser(String name, String type, String storeCategory) {
		String endpoint = "/api/suggest/create";
		
		Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("type", type);
        data.put("storeCategory", storeCategory);
		
        return postDataToFastApi(endpoint, data);
	}
	
	/**
	 * OCR 실행
	 * @param file 전송 파일
	 * @return 텍스트 추출 결과
	 * @throws IOException 
	 */
	public ResponseEntity<String> requestOcr(MultipartFile file) throws IOException{
		try {
			String endpoint = "/api/ocr";
			String url = fastApiBaseUrl + endpoint;

			// MultiValueMap으로 multipart 데이터 구성
			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", new MultipartFileResource(file));
			    
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			    
			HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
			    
			return restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		} catch (Exception e) {
			// ML 서비스 연결 실패 시 더 명확한 에러 메시지
			System.err.println("Failed to connect to ML service at " + fastApiBaseUrl + ": " + e.getMessage());
			throw new RuntimeException("ML 서비스 연결 실패 (503): " + e.getMessage(), e);
		}
	}

	public String requestTranslate(String text, String targetLanguage) {
		if (targetLanguage.equals("ko")) return text; // 원문 그대로 반환
		
		String endpoint = "/api/translate";
		
		Map<String, Object> data = new HashMap<>();
        data.put("text", text);
        data.put("targetLanguage", targetLanguage);
        	
        ResponseEntity<String> response = postDataToFastApi(endpoint, data);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                return jsonNode.get("translated").asText();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return text; // 번역 실패 시 원문 그대로 반환
	}

	public Map<String, Object> translateTexts(Map<String, Object> request) {
		try {
	        String endpoint = "/api/translateWeb";
	        String url = fastApiBaseUrl + endpoint;

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON);

	        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(request, headers);

	        ResponseEntity<Map> response = restTemplate.exchange(
	            url, HttpMethod.POST, requestEntity, Map.class);

	        return response.getBody();
	    } catch (Exception e) {
	        throw new RuntimeException("FastAPI 번역 서비스 연결 실패: " + e.getMessage(), e);
	    }
		
	}

} 
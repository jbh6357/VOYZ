package com.voiz.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.voiz.mapper.SpecialDayRepository;
import com.voiz.mapper.SpecialDayCategoryRepository;
import com.voiz.util.SpecialDayApi;
import com.voiz.vo.SpecialDay;
import com.voiz.vo.SpecialDayCategory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CollectorService {

	@Autowired
	private SpecialDayRepository specialDayRepository; 
	
	@Autowired
	private SpecialDayCategoryRepository specialDayCategoryRepository;
	
	@Autowired
	private SpecialDayApi specialDayApi;
	
	@Autowired
	private FastApiClient fastApiClient;
	
	private final ObjectMapper objectMapper = new ObjectMapper();
	
	public boolean collect(String year) {
		Map<String, String> endpoints = Map.of(
		        "공휴일", "getRestDeInfo",
		        "국경일", "getHoliDeInfo",
		        "절기", "get24DivisionsInfo",
		        "기념일", "getAnniversaryInfo",
		        "잡절", "getSundryDayInfo"
		    );
		
//		List<SpecialDay> allSpecialDays = new ArrayList<>();
		Map<String, SpecialDay> specialDayMap = new HashMap<>();
        try {
        	for (Map.Entry<String, String> entry : endpoints.entrySet()) {
        		for (int i = 1; i <= 12; i++) {
        		    String month = String.format("%02d", i); // "01", "02", ..., "12"
        		    String type = entry.getKey();       // "공휴일"
            	    String endpoint = entry.getValue();     // "getRestDeInfo"
            	    List<SpecialDay> result = specialDayApi.getSpecialDay(year, month, endpoint, type);
            	    
            	    for (SpecialDay day : result) {
            	    	 String key = day.getName();
            	    	 if (specialDayMap.containsKey(key)) {
            	                SpecialDay existing = specialDayMap.get(key);
            	                // 종료일 갱신: 더 큰 endDate 저장
            	                if (day.getEndDate().isAfter(existing.getEndDate())) {
            	                    existing.setEndDate(day.getEndDate());
            	                }
            	                
            	            } else {
            	                // 처음 들어오는 데이터는 복사해서 저장
            	                specialDayMap.put(key, day);
            	            }
            	    }
//            	    allSpecialDays.addAll(result);
        		} 		
        	}
        	// 최종 리스트로 변환
        	List<SpecialDay> allSpecialDays = new ArrayList<>(specialDayMap.values());
        	
        	// 각 특일에 대해 content 생성
        	for (SpecialDay specialDay : allSpecialDays) {
        		try {
        			String content = generateContentForSpecialDay(
        				specialDay.getName(), 
        				specialDay.getType(), 
        				specialDay.getCategory()
        			);
        			specialDay.setContent(content);
        		} catch (Exception e) {
        			// content 생성 실패 시 기본값 설정
        			specialDay.setContent(specialDay.getName() + "에 대한 마케팅 기회들을 확인해보세요.");
        			System.err.println("Content 생성 실패 for " + specialDay.getName() + ": " + e.getMessage());
        		}
        	}
        	
        	// 특일 데이터 저장
			List<SpecialDay> savedSpecialDays = specialDayRepository.saveAll(allSpecialDays);
			
			// 각 특일에 대해 카테고리 분류 및 저장
			for (SpecialDay specialDay : savedSpecialDays) {
				try {
					List<String> categories = classifySpecialDayCategories(
						specialDay.getName(), 
						specialDay.getType(), 
						specialDay.getCategory()
					);
					
					// 분류된 카테고리들을 DB에 저장
					saveCategoriesForSpecialDay((long)specialDay.getSdIdx(), categories);
					
				} catch (Exception e) {
					System.err.println("카테고리 분류 실패 for " + specialDay.getName() + ": " + e.getMessage());
				}
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}
	
	/**
	 * FastAPI를 통해 특일 컨텐츠 생성
	 * @param name 특일명
	 * @param type 특일 유형
	 * @param category 카테고리
	 * @return 생성된 컨텐츠
	 */
	private String generateContentForSpecialDay(String name, String type, String category) {
		try {
			System.out.println("컨텐츠 생성 요청: " + name + ", " + type + ", " + category);
			var response = fastApiClient.generateSpecialDayContent(name, type, category);
			
			System.out.println("FastAPI 응답 상태: " + response.getStatusCode());
			System.out.println("FastAPI 응답 내용: " + response.getBody());
			
			if (response.getStatusCode().is2xxSuccessful()) {
				JsonNode jsonResponse = objectMapper.readTree(response.getBody());
				if (jsonResponse.has("success") && jsonResponse.get("success").asBoolean()) {
					String content = jsonResponse.get("content").asText();
					System.out.println("생성된 컨텐츠: " + content);
					return content;
				}
			}
			
			// 실패 시 기본 메시지
			System.out.println("API 호출 실패, 기본 메시지 사용");
			return name + "에 대한 마케팅 기회들을 확인해보세요.";
			
		} catch (Exception e) {
			System.err.println("FastAPI 컨텐츠 생성 실패: " + e.getMessage());
			e.printStackTrace();
			return name + "에 대한 마케팅 기회들을 확인해보세요.";
		}
	}
	
	/**
	 * FastAPI를 통해 특일 카테고리 분류
	 * @param name 특일명
	 * @param type 특일 유형
	 * @param category 카테고리
	 * @return 분류된 카테고리 목록
	 */
	private List<String> classifySpecialDayCategories(String name, String type, String category) {
		try {
			System.out.println("카테고리 분류 요청: " + name + ", " + type + ", " + category);
			var response = fastApiClient.classifySpecialDayCategories(name, type, category);
			
			System.out.println("카테고리 분류 응답 상태: " + response.getStatusCode());
			System.out.println("카테고리 분류 응답 내용: " + response.getBody());
			
			if (response.getStatusCode().is2xxSuccessful()) {
				JsonNode jsonResponse = objectMapper.readTree(response.getBody());
				if (jsonResponse.has("success") && jsonResponse.get("success").asBoolean()) {
					List<String> categories = new ArrayList<>();
					JsonNode categoriesNode = jsonResponse.get("categories");
					
					if (categoriesNode.isArray()) {
						for (JsonNode categoryNode : categoriesNode) {
							categories.add(categoryNode.asText());
						}
					}
					
					System.out.println("분류된 카테고리: " + categories);
					return categories;
				}
			}
			
			// 실패 시 기본 카테고리
			System.out.println("카테고리 분류 실패, 기본 카테고리 사용");
			return List.of("한식");
			
		} catch (Exception e) {
			System.err.println("FastAPI 카테고리 분류 실패: " + e.getMessage());
			e.printStackTrace();
			return List.of("한식");
		}
	}
	
	/**
	 * 특일에 대한 카테고리들을 DB에 저장
	 * @param sdIdx 특일 ID
	 * @param categories 카테고리 목록
	 */
	private void saveCategoriesForSpecialDay(Long sdIdx, List<String> categories) {
		try {
			// 기존 카테고리 삭제 (중복 방지)
			specialDayCategoryRepository.deleteBySdIdx(sdIdx);
			
			// 새로운 카테고리들 저장
			List<SpecialDayCategory> categoryEntities = new ArrayList<>();
			for (String category : categories) {
				SpecialDayCategory categoryEntity = new SpecialDayCategory(sdIdx, category);
				categoryEntities.add(categoryEntity);
			}
			
			specialDayCategoryRepository.saveAll(categoryEntities);
			System.out.println("특일 " + sdIdx + "에 대한 카테고리 저장 완료: " + categories);
			
		} catch (Exception e) {
			System.err.println("카테고리 저장 실패 for sdIdx " + sdIdx + ": " + e.getMessage());
			e.printStackTrace();
		}
	}
	
}

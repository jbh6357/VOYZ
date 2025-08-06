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
			// 1단계: 음식과 명백히 무관한 특일 사전 필터링
			if (isNonFoodRelated(name, type)) {
				System.out.println("음식 무관 특일로 판단, 분류 건너뜀: " + name);
				return List.of(); // 빈 리스트 반환 = 매칭하지 않음
			}
			
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
					
					// 2단계: FastAPI 결과 검증 - 상식적으로 맞지 않는 분류 제거
					List<String> validatedCategories = validateFoodRelatedCategories(name, categories);
					System.out.println("검증된 카테고리: " + validatedCategories);
					return validatedCategories;
				}
			}
			
			// 실패 시 - 음식 관련 특일이면 기본 카테고리, 아니면 빈 리스트
			if (isFoodRelated(name, type)) {
				System.out.println("카테고리 분류 실패, 음식 관련으로 판단하여 기본 카테고리 사용");
				return List.of("한식");
			} else {
				System.out.println("카테고리 분류 실패, 음식 무관으로 판단하여 매칭 안함");
				return List.of();
			}
			
		} catch (Exception e) {
			System.err.println("FastAPI 카테고리 분류 실패: " + e.getMessage());
			e.printStackTrace();
			// 예외 발생 시에도 음식 관련성 체크
			return isFoodRelated(name, type) ? List.of("한식") : List.of();
		}
	}
	
	/**
	 * 음식과 명백히 무관한 특일인지 판단
	 * @param name 특일명
	 * @param type 특일 유형
	 * @return 음식과 무관하면 true
	 */
	private boolean isNonFoodRelated(String name, String type) {
		// 음식과 명백히 무관한 키워드들
		String[] nonFoodKeywords = {
			"보안", "정보", "IT", "컴퓨터", "인터넷", "사이버", "디지털", "통신", "네트워크",
			"의료", "병원", "의사", "간호", "치료", "약사", "건강검진",
			"교육", "학교", "대학", "교사", "학생", "교수", "학습", "시험",
			"법무", "법률", "변호사", "판사", "검사", "법원", "재판",
			"경찰", "소방", "군인", "국방", "치안", "방범", "순찰",
			"건설", "토목", "건축", "공학", "기술", "산업", "제조",
			"금융", "은행", "증권", "보험", "투자", "경제", "회계",
			"교통", "도로", "철도", "항공", "해운", "운송", "물류",
			"환경", "생태", "자연보호", "오염", "재활용", "에너지",
			"스포츠", "체육", "운동", "올림픽", "경기", "선수",
			"과학", "연구", "실험", "발명", "특허", "기술개발"
		};
		
		String nameUpper = name.toUpperCase();
		String typeUpper = type.toUpperCase();
		
		for (String keyword : nonFoodKeywords) {
			if (nameUpper.contains(keyword.toUpperCase()) || typeUpper.contains(keyword.toUpperCase())) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 음식과 관련있는 특일인지 판단
	 * @param name 특일명
	 * @param type 특일 유형
	 * @return 음식과 관련있으면 true
	 */
	private boolean isFoodRelated(String name, String type) {
		// 음식과 관련있는 키워드들
		String[] foodKeywords = {
			"음식", "요리", "식품", "맛", "먹", "식사", "밥", "국", "찌개", "죽", "면", "빵", "떡", "과자",
			"치킨", "피자", "햄버거", "커피", "차", "음료", "술", "맥주", "와인", "막걸리",
			"축제", "잔치", "파티", "기념일", "명절", "추석", "설날", "크리스마스", "생일",
			"봄", "여름", "가을", "겨울", "계절", "절기", "입춘", "입하", "입추", "입동",
			"추수", "수확", "농업", "농민", "쌀", "곡식", "채소", "과일", "생선", "고기",
			"전통", "문화", "민속", "향토", "지역", "특산", "맛집", "미식"
		};
		
		String nameUpper = name.toUpperCase();
		String typeUpper = type.toUpperCase();
		
		for (String keyword : foodKeywords) {
			if (nameUpper.contains(keyword.toUpperCase()) || typeUpper.contains(keyword.toUpperCase())) {
				return true;
			}
		}
		
		// 기념일, 공휴일, 절기는 일반적으로 음식과 관련성이 있음
		if (type.contains("기념일") || type.contains("공휴일") || type.contains("절기")) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * FastAPI 분류 결과를 검증하여 비합리적인 매칭 제거
	 * @param name 특일명
	 * @param categories FastAPI가 분류한 카테고리들
	 * @return 검증된 카테고리 목록
	 */
	private List<String> validateFoodRelatedCategories(String name, List<String> categories) {
		// 음식 관련 카테고리만 허용
		String[] allowedCategories = {"한식", "중식", "일식", "양식", "카페", "치킨", "피자", "버거", "분식"};
		List<String> allowedList = List.of(allowedCategories);
		
		List<String> validCategories = categories.stream()
			.filter(allowedList::contains)
			.collect(java.util.stream.Collectors.toList());
		
		// 검증 실패한 경우 로그 출력
		if (validCategories.size() != categories.size()) {
			List<String> rejected = categories.stream()
				.filter(cat -> !allowedList.contains(cat))
				.collect(java.util.stream.Collectors.toList());
			System.out.println("부적절한 카테고리 제거됨 for " + name + ": " + rejected);
		}
		
		return validCategories;
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
			
			// 빈 카테고리 리스트인 경우 (음식 무관 특일) 저장하지 않음
			if (categories.isEmpty()) {
				System.out.println("특일 " + sdIdx + "은 음식 무관으로 분류되어 카테고리 저장 안함");
				return;
			}
			
			// 새로운 카테고리들 저장
			List<SpecialDayCategory> categoryEntities = new ArrayList<>();
			for (String category : categories) {
				SpecialDayCategory categoryEntity = new SpecialDayCategory(sdIdx, category);
				categoryEntities.add(categoryEntity);
			}
			
			if (!categoryEntities.isEmpty()) {
				specialDayCategoryRepository.saveAll(categoryEntities);
				System.out.println("특일 " + sdIdx + "에 대한 카테고리 저장 완료: " + categories);
			}
			
		} catch (Exception e) {
			System.err.println("카테고리 저장 실패 for sdIdx " + sdIdx + ": " + e.getMessage());
			e.printStackTrace();
		}
	}
	
}

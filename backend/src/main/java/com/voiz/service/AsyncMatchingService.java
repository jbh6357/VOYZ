package com.voiz.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiz.mapper.SpecialDayMatchRepository;
import com.voiz.mapper.SpecialDayCategoryRepository;
import com.voiz.mapper.SpecialDayRepository;
import com.voiz.mapper.SpecialDaySuggestRepository;
import com.voiz.mapper.CalendarRepository;
import com.voiz.vo.SpecialDayMatch;
import com.voiz.vo.SpecialDaySuggest;
import com.voiz.vo.SpecialDay;
import com.voiz.vo.SpecialDayCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AsyncMatchingService {

    @Autowired
    private SpecialDayMatchRepository specialDayMatchRepository;
    
    @Autowired
    private SpecialDayCategoryRepository specialDayCategoryRepository;
    
    @Autowired
    private SpecialDayRepository specialDayRepository;
    
    @Autowired
    private SpecialDaySuggestRepository specialDaySuggestRepository;
    
    @Autowired
    private CalendarRepository calendarRepository;
    
    @Autowired
    private FastApiClient fastApiClient;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 비동기로 매칭과 제안 생성 모두 처리
     */
    @Async("taskExecutor")
    public void processUserMatchingAndSuggestionsAsync(String userId, String storeCategory) {
        System.out.println("=== 비동기 매칭 및 제안 생성 시작 ===");
        System.out.println("사용자: " + userId + ", 업종: " + storeCategory);
        
        try {
            // 1. 특일 매칭
            List<SpecialDayMatch> matchesToSave = matchSpecialDaysForUser(userId, storeCategory);
            
            // 2. 제안 생성
            if(matchesToSave != null && !matchesToSave.isEmpty()) {
                System.out.println("매칭 완료, 제안 생성 시작 - 매칭 수: " + matchesToSave.size());
                
                for(SpecialDayMatch match : matchesToSave) {
                    try {
                        createSpecialDaySugForUser(
                            match.getSm_idx(), 
                            match.getSd_idx(), 
                            userId, 
                            storeCategory
                        );
                        System.out.println("제안 생성 완료: SM_IDX=" + match.getSm_idx());
                    } catch (Exception e) {
                        System.err.println("제안 생성 실패: SM_IDX=" + match.getSm_idx() + ", 오류: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("매칭된 특일이 없어 제안 생성을 건너뜁니다.");
            }
        } catch (Exception e) {
            System.err.println("비동기 처리 전체 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== 비동기 매칭 및 제안 생성 완료 ===");
    }
    
    /**
     * 유저의 업종에 맞는 특일들을 매칭 테이블에 저장
     */
    private List<SpecialDayMatch> matchSpecialDaysForUser(String userId, String storeCategory) {
        try {
            System.out.println("유저 " + userId + "의 업종 " + storeCategory + "에 맞는 특일 매칭 시작");
            
            // 1. 해당 업종 카테고리에 맞는 특일 카테고리 데이터 조회
            List<SpecialDayCategory> matchingCategories = specialDayCategoryRepository.findByCategory(storeCategory);
            
            System.out.println("매칭되는 카테고리 데이터 수: " + matchingCategories.size());
            
            // 2. 매칭되는 특일들을 SpecialDayMatch 테이블에 저장
            List<SpecialDayMatch> matchesToSave = new ArrayList<>();
            
            for (SpecialDayCategory categoryData : matchingCategories) {
                Long sdIdx = categoryData.getSdIdx();
                
                // 중복 확인
                if (!specialDayMatchRepository.existsByUserIdAndSd_idx(userId, sdIdx.intValue())) {
                    SpecialDayMatch match = new SpecialDayMatch();
                    match.setSd_idx(sdIdx.intValue());
                    match.setUserId(userId);
                    
                    matchesToSave.add(match);
                }
            }
            
            // 3. 일괄 저장
            if (!matchesToSave.isEmpty()) {
                specialDayMatchRepository.saveAll(matchesToSave);            	
                System.out.println("유저 " + userId + "에 대해 " + matchesToSave.size() + "개의 특일 매칭 완료");
                return matchesToSave;
            } else {
                System.out.println("유저 " + userId + "에 대해 매칭할 새로운 특일이 없습니다");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("특일 매칭 실패 for userId " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 특일 제안 생성
     */
    private void createSpecialDaySugForUser(int sm_idx, int sd_idx, String userId, String storeCategory) {
        try {
            Optional<SpecialDay> optionalDay = specialDayRepository.findById(sd_idx);
            
            if (optionalDay.isEmpty()) {
                throw new RuntimeException("잘못된 특일 정보");
            }
            
            SpecialDay specialDay = optionalDay.get();
            String name = specialDay.getName();
            String type = specialDay.getType();
            
            System.out.println("제안 생성 요청: " + name + ", " + type + ", " + storeCategory);
            var response = fastApiClient.createSpecialDaySugForUser(name, type, storeCategory);
            
            System.out.println("제안 생성 응답 상태: " + response.getStatusCode());
            System.out.println("제안 생성 응답 내용: " + response.getBody());
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                if (jsonResponse.has("success") && jsonResponse.get("success").asBoolean()) {
                    // 1. SpecialDaySuggest 객체로 변환
                    SpecialDaySuggest suggest = objectMapper.treeToValue(jsonResponse, SpecialDaySuggest.class);
                    
                    // 2. 필수 누락된 값 보완
                    suggest.setStartDate(specialDay.getStartDate());  
                    suggest.setEndDate(specialDay.getEndDate());
                    suggest.setContent(specialDay.getContent());
                    suggest.setSmIdx(sm_idx);
                    suggest.setCalendarIdx(calendarRepository.findCalendarIdxByUserId(userId));
                    
                    // 3. 저장
                    if (!specialDaySuggestRepository.existsBySmIdx(suggest.getSmIdx())) {
                        if(suggest.getTitle()!=null) {
                            specialDaySuggestRepository.save(suggest);
                        }
                    }
                }
            }
            
        }catch(Exception e){
            System.err.println("FastAPI 제안 생성 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
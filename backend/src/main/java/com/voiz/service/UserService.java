package com.voiz.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiz.dto.LoginRequestDto;
import com.voiz.dto.LoginResponseDto;
import com.voiz.dto.UserRegistrationDto;
import com.voiz.mapper.UsersRepository;
import com.voiz.mapper.SpecialDayMatchRepository;
import com.voiz.mapper.SpecialDayRepository;
import com.voiz.mapper.SpecialDaySuggestRepository;
import com.voiz.mapper.CalendarRepository;
import com.voiz.mapper.ReminderRepository;
import com.voiz.mapper.SpecialDayCategoryRepository;
import com.voiz.vo.Users;
import com.voiz.vo.SpecialDayMatch;
import com.voiz.vo.SpecialDaySuggest;
import com.voiz.vo.Calendar;
import com.voiz.vo.Reminder;
import com.voiz.vo.SpecialDay;
import com.voiz.vo.SpecialDayCategory;
import com.voiz.util.PasswordEncoder;
import com.voiz.service.JwtTokenService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

@Service
@Transactional
public class UserService {

    private final CollectorService collectorService;

    @Autowired
    private UsersRepository usersRepository;
    
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
    private ReminderRepository reminderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenService jwtTokenService;
    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
	private FastApiClient fastApiClient;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    UserService(CollectorService collectorService) {
        this.collectorService = collectorService;
    }

    private boolean existsByUserIdNative(String userId) {
        try {
            String sql = "SELECT COUNT(*) FROM VOYZ_USERS WHERE USER_ID = ?";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, userId);
            BigDecimal count = (BigDecimal) query.getSingleResult();
            return count.intValue() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean registerUser(UserRegistrationDto registrationDto) {
        if (existsByUserIdNative(registrationDto.getUserId())) {
            return false;
        }

        // 1. 하이픈 제거
        String rawPhone = registrationDto.getUserPhone();
        String cleanedPhone = rawPhone.replaceAll("-", "");
        
        // 2. 숫자만으로 구성되어 있고 길이가 11이 아니면 실패
        if (!cleanedPhone.matches("\\d{11}")) {
            return false;
        }
        
        Users users = new Users();
        users.setUserId(registrationDto.getUserId());
        users.setUserPw(passwordEncoder.encode(registrationDto.getUserPw()));

        users.setUserName(registrationDto.getUserName());
        users.setStoreName(registrationDto.getStoreName());
        users.setUserPhone(cleanedPhone);
        users.setStoreCategory(registrationDto.getStoreCategory());
        users.setStoreAddress(registrationDto.getStoreAddress());

        try {
            usersRepository.save(users);
            Calendar c = new Calendar();
            Reminder r = new Reminder();
            r.setUserId(users.getUserId());
            c.setUserId(users.getUserId());
            calendarRepository.save(c);
            reminderRepository.save(r);
            
            // 회원가입 성공 후 업종에 맞는 특일 매칭
            List<SpecialDayMatch> matchesToSave= matchSpecialDaysForUser(registrationDto.getUserId(), registrationDto.getStoreCategory());
            
            if(matchesToSave!=null) {
            	for(SpecialDayMatch match : matchesToSave) {
            		createSpecialDaySugForUser(
            				match.getSm_idx(), 
            				match.getSd_idx(), 
            				registrationDto.getUserId(), 
            				registrationDto.getStoreCategory()
            		);
            	}
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

	public Optional<Users> getUserByUsername(String username) {
        return usersRepository.findByUserName(username);
    }

    public LoginResponseDto login(LoginRequestDto loginDto) {

        Optional<Users> optionalUser = usersRepository.findByUserId(loginDto.getUserId());

        if (optionalUser.isEmpty()
                || !passwordEncoder.matches(loginDto.getUserPw(), optionalUser.get().getUserPw())) {
            throw new RuntimeException("잘못된 로그인 정보");
        }
        
        Users users = optionalUser.get();

        Map<String, String> tokens = jwtTokenService.generateTokens(users);

        LoginResponseDto loginResponseDto = new LoginResponseDto();
        
        loginResponseDto.setUserId(users.getUserId());
        loginResponseDto.setStoreName(users.getStoreName());
        loginResponseDto.setStoreCategory(users.getStoreCategory());
        loginResponseDto.setUserName(users.getUserName());
        loginResponseDto.setAccessToken(tokens.get("accessToken"));
        loginResponseDto.setRefreshToken(tokens.get("refreshToken"));
        loginResponseDto.setTokenType(tokens.get("tokenType"));

        return loginResponseDto;

    }
    
    /**
     * 유저의 업종에 맞는 특일들을 매칭 테이블에 저장
     * @param userId 유저 ID
     * @param storeCategory 업종 (한식, 중식, 일식, 양식, 카페, 치킨, 피자, 버거, 분식)
     * @return 
     */
    private List<SpecialDayMatch> matchSpecialDaysForUser(String userId, String storeCategory) {
        try {
            System.out.println("유저 " + userId + "의 업종 " + storeCategory + "에 맞는 특일 매칭 시작");
            
            // 1. 해당 업종 카테고리에 맞는 특일 카테고리 데이터 조회
            List<SpecialDayCategory> matchingCategories = specialDayCategoryRepository.findByCategory(storeCategory);
            
            System.out.println("매칭되는 카테고리 데이터 수: " + matchingCategories.size());
            
            // 2. 중복 방지를 위해 기존 매칭 데이터 삭제 (필요시)
            // specialDayMatchRepository.deleteByUserId(userId);
            
            // 3. 매칭되는 특일들을 SpecialDayMatch 테이블에 저장
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
            
            // 4. 일괄 저장
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
     * 
     * @param sm_idx
     * @param sd_idx
     * @param userId
     * @param storeCategory
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
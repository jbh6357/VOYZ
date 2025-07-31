package com.voiz.service;

import com.voiz.dto.LoginRequestDto;
import com.voiz.dto.LoginResponseDto;
import com.voiz.dto.UserRegistrationDto;
import com.voiz.mapper.UsersRepository;
import com.voiz.mapper.CalendarRepository;
import com.voiz.mapper.ReminderRepository;
import com.voiz.vo.Users;
import com.voiz.vo.Calendar;
import com.voiz.vo.Reminder;
import com.voiz.util.PasswordEncoder;
import com.voiz.service.JwtTokenService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final CollectorService collectorService;

    @Autowired
    private UsersRepository usersRepository;
    
    
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
    private AsyncMatchingService asyncMatchingService;

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

    @Transactional
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
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 트랜잭션 완료 후 비동기 처리를 위한 별도 메서드
     */
    public void startAsyncProcessing(String userId, String storeCategory) {
        System.out.println("=== 회원가입 완료, 비동기 처리 시작 ===");
        asyncMatchingService.processUserMatchingAndSuggestionsAsync(userId, storeCategory);
        System.out.println("=== 비동기 호출 완료 ===");
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
    
    

}
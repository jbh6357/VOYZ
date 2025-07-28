package com.voiz.service;

import com.voiz.config.JwtTokenProvider;
import com.voiz.dto.LoginRequestDto;
import com.voiz.dto.LoginResponseDto;
import com.voiz.dto.UserRegistrationDto;
import com.voiz.mapper.TokenRepository;
import com.voiz.mapper.UsersRepository;
import com.voiz.vo.TokenEntity;
import com.voiz.vo.Users;
import com.voiz.util.PasswordEncoder;
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

@Service
@Transactional
public class UserService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private boolean existsByUsernameNative(String username) {
        try {
            String sql = "SELECT COUNT(*) FROM USERS WHERE USERNAME = ?";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, username);
            BigDecimal count = (BigDecimal) query.getSingleResult();
            return count.intValue() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean existsByEmailNative(String email) {
        try {
            String sql = "SELECT COUNT(*) FROM USERS WHERE EMAIL = ?";
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, email);
            BigDecimal count = (BigDecimal) query.getSingleResult();
            return count.intValue() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, String> loginAndIssueTokens(LoginRequestDto loginDto) {
        // 1. findById로 사용자를 조회합니다.
        Optional<Users> optionalUser = usersRepository.findById(loginDto.getUsername());

        if (optionalUser.isEmpty()
                || !passwordEncoder.matches(loginDto.getPassword(), optionalUser.get().getUserPw())) {
            throw new RuntimeException("잘못된 로그인 정보");
        }

        Users users = optionalUser.get();

        // 2. access 토큰 생성하기

        String accessUuid = UUID.randomUUID().toString();


        String accessToken = jwtTokenProvider.createAccessToken(
                users.getUserName(), users.getRole(), accessUuid, users.getUserId());

        // 3. Refresh Token 생성 
        //String refreshToken = jwtTokenProvider.createRefreshToken(users.getUserName());

        // 4. TokenEntity에 모든 정보 저장
        TokenEntity token = TokenEntity.builder()
                .users(users) 
                .accessUuid(accessUuid)
                //.refreshToken(refreshToken) 
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        tokenRepository.save(token);

        // 5. Access Token과 Refresh Token을 모두 반환
        return Map.of(
                "accessToken", accessToken
                //"refreshToken", refreshToken
                );
    }

    public boolean registerUser(UserRegistrationDto registrationDto) {
        if (existsByUsernameNative(registrationDto.getUsername())) {
            return false;
        }

        if (registrationDto.getEmail() != null && existsByEmailNative(registrationDto.getEmail())) {
            return false;
        }

        Users users = new Users();
        users.setUserId(registrationDto.getUsername());
        users.setUserName(registrationDto.getUsername());

        users.setUserPw(passwordEncoder.encode(registrationDto.getPassword()));
        users.setUserEmail(registrationDto.getEmail());
        users.setUserName(registrationDto.getName());
        users.setStoreName(registrationDto.getStoreName());
        users.setUserPhone(registrationDto.getPhone());
        users.setStoreCategory(registrationDto.getStoreCategory());
        users.setStoreAddress(registrationDto.getStoreAddress());

        users.setRole(registrationDto.getRole() != null ? registrationDto.getRole() : "USERS");

        try {
            usersRepository.save(users);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Optional<Users> getUserByUsername(String username) {
        return usersRepository.findByUserName(username);
    }

    // JWT 토큰 생성
    public Map<String, String> generateAndStoreTokens(Users users) {
        String accessUuid = UUID.randomUUID().toString();
        String accessToken = jwtTokenProvider.createAccessToken(
                users.getUserName(), users.getRole(), accessUuid, users.getUserId());

        // Refresh Token 만료 시간 계산 (7일 기준)
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        TokenEntity token = TokenEntity.builder()
                .users(users)
                .accessUuid(accessUuid)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .build();

        tokenRepository.save(token);

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", accessToken);

        return tokenMap;
    }

}
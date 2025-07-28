package com.voiz.service;

import com.voiz.config.JwtTokenProvider;
import com.voiz.dto.LoginRequestDto;
import com.voiz.dto.LoginResponseDto;
import com.voiz.dto.UserRegistrationDto;
import com.voiz.mapper.TokenRepository;
import com.voiz.mapper.UserRepository;
import com.voiz.vo.TokenEntity;
import com.voiz.vo.Users;
import com.voiz.util.PasswordEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private UserRepository userRepository;

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

    public LoginResponseDto login(LoginRequestDto loginRequest) {
        Optional<Users> userOpt = userRepository.findByUserName(loginRequest.getUsername());

        if (userOpt.isPresent()) {
            Users users = userOpt.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), users.getUserPw())) {
                // 실제 프로젝트에서는 JWT 토큰을 생성해야 합니다
                String token = "dummy-token-" + System.currentTimeMillis();

                return new LoginResponseDto(
                        token,
                        users.getUserName(),
                        users.getStoreName(),
                        users.getRole(),
                        "로그인 성공");
            } else {
                return new LoginResponseDto(null, null, null, null, "비밀번호가 일치하지 않습니다.");
            }
        } else {
            return new LoginResponseDto(null, null, null, null, "사용자를 찾을 수 없습니다.");
        }
    }

    public boolean registerUser(UserRegistrationDto registrationDto) {
        if (existsByUsernameNative(registrationDto.getUsername())) {
            return false;
        }

        if (registrationDto.getEmail() != null && existsByEmailNative(registrationDto.getEmail())) {
            return false;
        }

        Users users = new Users();
        // ✅ 해결: 기본 키를 생성하고 설정합니다.
        users.setUserId(UUID.randomUUID().toString());

        users.setUserName(registrationDto.getUsername());
        users.setUserPw(passwordEncoder.encode(registrationDto.getPassword()));
        users.setUserEmail(registrationDto.getEmail());
        users.setStoreName(registrationDto.getName());
        users.setRole(registrationDto.getRole() != null ? registrationDto.getRole() : "USER");
        users.setUserPhone(registrationDto.getPhone());
        users.setStoreCategory(registrationDto.getStoreCategory());
        users.setStoreAddress(registrationDto.getStoreAddress());

        // ✅ 해결: Users 엔티티에서 NOT NULL인 필드들을 처리해야 합니다.
        // 일단은 이 필드들이 nullable 하다고 가정하고 진행하겠습니다.
        // 만약 필수 필드라면 UserRegistrationDto에 추가해야 합니다.
        // users.setUserPhone(registrationDto.getPhone());
        // users.setStoreCategory(registrationDto.getCategory());
        // users.setStoreAddress(registrationDto.getAddress());

        try {
            userRepository.save(users);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Optional<Users> getUserByUserName(String username) {
        return userRepository.findByUserName(username);
    }

    public boolean deleteUser(LoginRequestDto loginRequest) {
        // 1) 사용자 존재 여부 확인
        Optional<Users> userOpt = userRepository.findByUserName(loginRequest.getUsername());
        if (userOpt.isEmpty()) {
            // 존재하지 않는 계정
            return false;
        }

        Users users = userOpt.get();

        // 2) 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(loginRequest.getPassword(), users.getUserPw())) {
            // 비밀번호 불일치
            return false;
        }

        try {
            // 3) 삭제 수행
            userRepository.delete(users); // JPA 기본 delete 메서드
            return true;
        } catch (Exception e) {
            e.printStackTrace(); // 장애 시 원인 파악을 위해 로그 남김
            return false;
        }
    }

    public Map<String, String> generateAndStoreTokens(Users user) {
        String accessUuid = UUID.randomUUID().toString();
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserName(), user.getRole(), accessUuid, user.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserName());

        // Refresh Token 만료 시간 계산 (7일 기준)
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        TokenEntity token = TokenEntity.builder()
                .user(user)
                .accessUuid(accessUuid)
                .refreshToken(refreshToken)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .build();

        tokenRepository.save(token);

        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("accessToken", accessToken);
        tokenMap.put("refreshToken", refreshToken);

        return tokenMap;
    }

    public ResponseEntity<?> refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("리프레시 토큰이 유효하지 않음");
        }

        Optional<TokenEntity> tokenOpt = tokenRepository.findByRefreshToken(refreshToken);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰 정보 없음");
        }

        TokenEntity token = tokenOpt.get();
        Users user = token.getUser();

        // 기존 access uuid 무효화 → 필요시 삭제 or 재사용
        String newAccessUuid = UUID.randomUUID().toString();
        String newAccessToken = jwtTokenProvider.createAccessToken(
                user.getUserName(), user.getRole(), newAccessUuid, user.getUserId());

        token.setAccessUuid(newAccessUuid);
        token.setLastUsedAt(LocalDateTime.now());
        tokenRepository.save(token);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    public Map<String, String> loginAndIssueTokens(LoginRequestDto loginDto) {
        Optional<Users> optionalUser = userRepository.findByUserName(loginDto.getUsername());

        if (optionalUser.isEmpty()
                || !passwordEncoder.matches(loginDto.getPassword(), optionalUser.get().getUserPw())) {
            throw new RuntimeException("잘못된 로그인 정보");
        }

        Users user = optionalUser.get();

        String accessUuid = UUID.randomUUID().toString();
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserName(), user.getRole(), accessUuid, user.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserName());

        // DB 저장
        TokenEntity token = TokenEntity.builder()
                .user(user)
                .accessUuid(accessUuid)
                .refreshToken(refreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .build();
        tokenRepository.save(token);

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken);
    }

}
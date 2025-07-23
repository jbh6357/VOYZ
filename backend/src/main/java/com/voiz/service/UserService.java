package com.voiz.service;
import com.voiz.dto.LoginRequestDto;
import com.voiz.dto.LoginResponseDto;
import com.voiz.dto.UserRegistrationDto;
import com.voiz.mapper.UserRepository;
import com.voiz.vo.User;
import com.voiz.util.PasswordEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PersistenceContext
    private EntityManager entityManager;
    
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
        Optional<User> userOpt = userRepository.findByUsername(loginRequest.getUsername());
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                // 실제 프로젝트에서는 JWT 토큰을 생성해야 합니다
                String token = "dummy-token-" + System.currentTimeMillis();
                
                return new LoginResponseDto(
                    token,
                    user.getUsername(),
                    user.getName(),
                    user.getRole(),
                    "로그인 성공"
                );
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
        
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setEmail(registrationDto.getEmail());
        user.setName(registrationDto.getName());
        user.setRole(registrationDto.getRole() != null ? registrationDto.getRole() : "USER");
        
        try {
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
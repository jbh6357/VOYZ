package com.voiz.service;

import com.voiz.dto.LoginRequestDto;
import com.voiz.dto.LoginResponseDto;
import com.voiz.dto.UserRegistrationDto;
import com.voiz.mapper.UsersRepository;
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

        Users users = new Users();
        users.setUserId(registrationDto.getUserId());
        users.setUserPw(passwordEncoder.encode(registrationDto.getUserPw()));

        users.setUserName(registrationDto.getUserName());
        users.setStoreName(registrationDto.getStoreName());
        users.setUserPhone(registrationDto.getUserPhone());
        users.setStoreCategory(registrationDto.getStoreCategory());
        users.setStoreAddress(registrationDto.getStoreAddress());

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

    public LoginResponseDto login(LoginRequestDto loginDto) {

        Optional<Users> optionalUser = usersRepository.findByUserId(loginDto.getUserId());

        if (optionalUser.isEmpty()
                || !passwordEncoder.matches(loginDto.getUserPw(), optionalUser.get().getUserPw())) {
            throw new RuntimeException("잘못된 로그인 정보");
        }
        
        Users users = optionalUser.get();

        LoginResponseDto loginResponseDto = new LoginResponseDto();
        
        loginResponseDto.setUserId(users.getUserId());
        loginResponseDto.setStoreName(users.getStoreName());
        loginResponseDto.setStoreCategory(users.getStoreCategory());
        loginResponseDto.setUserName(users.getUserName());

        return loginResponseDto;

    }

}
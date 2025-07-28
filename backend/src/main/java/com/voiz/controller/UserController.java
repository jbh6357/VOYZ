package com.voiz.controller;

import com.voiz.config.JwtTokenProvider;
import com.voiz.dto.LoginRequestDto;
import com.voiz.dto.LoginResponseDto;
import com.voiz.dto.UserRegistrationDto;
import com.voiz.mapper.UserRepository;
import com.voiz.service.UserService;
import com.voiz.util.PasswordEncoder;
import com.voiz.vo.Users;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "사용자 관리 API")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginDto) {
        try {
            Map<String, String> tokens = userService.loginAndIssueTokens(loginDto);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        // ✅ 해결: 모든 로직을 서비스 계층에 위임합니다.
        return userService.refreshAccessToken(refreshToken);
    }

    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public ResponseEntity<String> register(@RequestBody UserRegistrationDto registrationDto) {
        boolean success = userService.registerUser(registrationDto);

        if (success) {
            return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("회원가입에 실패했습니다. 이미 존재하는 사용자명 또는 이메일입니다.");
        }
    }

    @PostMapping("/delete")
    @Operation(summary = "회원탈퇴", description = "등록된 회원이 탈퇴합니다.")
    public ResponseEntity<String> delete(@RequestBody LoginRequestDto loginRequest) {
        boolean deleted = userService.deleteUser(loginRequest);

        if (deleted) {
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("회원 탈퇴에 실패했습니다. 사용자 정보를 확인해주세요.");
        }
    }

    @GetMapping("/test")
    @Operation(summary = "테스트", description = "API 연결 테스트")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API 연결 성공!");
    }
}
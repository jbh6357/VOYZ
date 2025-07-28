package com.voiz.controller;

import com.voiz.dto.LoginRequestDto;
import com.voiz.dto.LoginResponseDto;
import com.voiz.dto.UserRegistrationDto;
import com.voiz.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "user", description = "사용자 관리 API")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequest) {
        LoginResponseDto response = userService.login(loginRequest);
        
        if (response.getToken() != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
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
    
    @GetMapping("/test")
    @Operation(summary = "테스트", description = "API 연결 테스트")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API 연결 성공!");
    }
} 
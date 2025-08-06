package com.voiz.controller;

import com.voiz.service.JwtTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth", description = "인증 관리 API")
public class AuthController {

    @Autowired
    private JwtTokenService jwtTokenService;

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급합니다.")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Map<String, String> tokens = jwtTokenService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/auto-login")
    @Operation(summary = "자동 로그인", description = "Access Token을 검증하여 자동 로그인을 수행합니다.")
    public ResponseEntity<Map<String, Object>> autoLogin(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String accessToken = authHeader.substring(7);
            
            if (!jwtTokenService.validateToken(accessToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String userId = jwtTokenService.getUserIdFromToken(accessToken);
            String accessUuid = jwtTokenService.getAccessUuidFromToken(accessToken);
            
            jwtTokenService.updateLastUsedTime(accessUuid);

            Map<String, Object> response = Map.of(
                "message", "자동 로그인 성공",
                "userId", userId
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자의 모든 토큰을 무효화합니다.")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().build();
            }

            String accessToken = authHeader.substring(7);
            String userId = jwtTokenService.getUserIdFromToken(accessToken);
            
            jwtTokenService.revokeToken(userId);
            
            return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "로그아웃 처리 중 오류가 발생했습니다."));
        }
    }
}
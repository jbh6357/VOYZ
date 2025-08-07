package com.voiz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.voiz.dto.FcmTokenDto;
import com.voiz.service.FcmTokenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/fcm")
@Tag(name = "fcm", description = "제발 되라고")
public class FcmController {

	@Autowired
	private FcmTokenService fcmTokenService;

    @PostMapping("/token")
    @Operation(summary = "FCM 토큰 저장", description = "사용자 로그인 시 기기의 FCM 토큰을 서버에 저장합니다.")
    public ResponseEntity<Void> saveToken(@RequestBody FcmTokenDto dto) {
        fcmTokenService.save(dto);
        return ResponseEntity.ok().build();
    }
    
}

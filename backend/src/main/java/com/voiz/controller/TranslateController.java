package com.voiz.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.voiz.service.TranslateService;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("api/translate")
@Tag(name = "translate", description = "번역 API")
public class TranslateController {
	
	@Autowired TranslateService translateService;
	
	@PostMapping("/")
	@Operation(summary = "텍스트 번역", description = "FastAPI 서버를 통해 텍스트를 번역합니다.")
	public ResponseEntity<Map<String, Object>> translateText(
	    @RequestBody Map<String, Object> request) {
	    
	    try {
	        Map<String, Object> result = translateService.translateTexts(request);
	        return ResponseEntity.ok(result);
	    } catch (Exception e) {
	        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
	    }
	}
}

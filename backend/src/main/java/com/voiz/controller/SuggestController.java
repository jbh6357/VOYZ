package com.voiz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voiz.dto.MatchRequestDto;
import com.voiz.service.SuggestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/suggest")
@Tag(name = "Make Suggest", description = "사용자 기반 마케팅 제안 생성 API")
public class SuggestController {
	
	@Autowired
	private SuggestService suggestService;
	
	@PostMapping("/special-day")
	@Operation(summary = "특일데이터 기반 마케팅 제안", description = "사용자와 매칭된 특일 데이터를 기반으로 마케팅 제안을 생성하고 DB에 저장합니다.")
	public ResponseEntity<String> suggestBySpecialDay(@RequestBody MatchRequestDto matchRequest){
		suggestService.make(matchRequest);
		return null;
	}

}

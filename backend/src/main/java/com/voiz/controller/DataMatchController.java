package com.voiz.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voiz.dto.MatchRequestDto;
import com.voiz.service.MatchService;
import com.voiz.vo.SpecialDayMatch;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/match")
@Tag(name = "Data Match", description = "사용자-데이터 매칭 API")
public class DataMatchController {

	@Autowired
	private MatchService matchService;
	
	@PostMapping("/special-day")
	@Operation(summary = "특일데이터 매칭", description = "특일 데이터와 사용자의 업종 카테고리를 고려해서 연관성이 높을 시 매칭시켜 DB에 저장합니다.")
	public ResponseEntity<String> matchSpecialDay(@RequestBody MatchRequestDto matchRequest){
		List<SpecialDayMatch> result = matchService.match(matchRequest);
		boolean success = matchService.insert(result);
	    if (success) {
	        return ResponseEntity.ok().build();
	    } else {
	        return ResponseEntity.internalServerError().build();
	    }
	}
}

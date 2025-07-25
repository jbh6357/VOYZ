package com.voiz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.voiz.service.CollectorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/collect")
@Tag(name = "Data Collect", description = "데이터 수집 API")
public class DataCollectorController {
	
	@Autowired
	private CollectorService collectorService;
	
	@PostMapping("/special-day")
	@Operation(summary = "특일데이터수집", description = "한국천문연구원 특일 정보제공 서비스 API를 이용하여 특정년도의 특일 데이터를 수집하고 DB에 저장합니다.")
	public ResponseEntity<Void> collectSpecialDay(@RequestParam String year) {
	    boolean success = collectorService.collect(year);
	    if (success) {
	        return ResponseEntity.ok().build();
	    } else {
	        return ResponseEntity.internalServerError().build();
	    }
	}
}

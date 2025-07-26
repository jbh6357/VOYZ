package com.voiz.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiz.dto.MatchRequestDto;
import com.voiz.dto.MatchSpecialDayDto;
import com.voiz.mapper.SpecialDayMatchRepository;
import com.voiz.mapper.SpecialDayRepository;
import com.voiz.mapper.UsersRepository;
import com.voiz.vo.SpecialDayMatch;

@Service
public class MatchService {
	
	@Autowired
    private FastApiClient fastApiClient;
	
	@Autowired
	private SpecialDayRepository specialDayRepository;
	
	@Autowired
	private SpecialDayMatchRepository specialDayMatchRepository;
	
	public List<SpecialDayMatch> match(MatchRequestDto matchRequest) {
		List<MatchSpecialDayDto> specialDays = specialDayRepository.findForMatch();
		
		// Map으로 직접 구성
	    Map<String, Object> body = new HashMap<>();
	    body.put("matchRequest", matchRequest);
	    body.put("specialDays", specialDays);
	    try {
	    	ResponseEntity<String> response = fastApiClient.getMatchResult("specialDay", body);
	    	String responseBody = response.getBody();
	    	
	    	ObjectMapper objectMapper = new ObjectMapper();
	    	List<SpecialDayMatch> matches = objectMapper.readValue(responseBody, new TypeReference<List<SpecialDayMatch>>() {});
	    	
	    	return matches;
	    } catch (Exception e) {
	    	return null;
	    }
	}
	
	public boolean insert(List<SpecialDayMatch> specialDayMatch) {
		try {
	        specialDayMatchRepository.saveAll(specialDayMatch);
	        return true;
	    } catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	}
}

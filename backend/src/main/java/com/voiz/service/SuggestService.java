package com.voiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.voiz.dto.MatchRequestDto;
import com.voiz.mapper.SpecialDayMatchRepository;
import com.voiz.mapper.SpecialDayRepository;
import com.voiz.vo.SpecialDayMatch;

@Service
public class SuggestService {
	
	@Autowired
    private FastApiClient fastApiClient;
	
	@Autowired
	private SpecialDayRepository specialDayRepository;
	
	@Autowired
	private SpecialDayMatchRepository specialDayMatchRepository; 
	
	public void make(MatchRequestDto matchRequest) {
		List<SpecialDayMatch> result = specialDayMatchRepository.findByUserId(matchRequest.getUserId());
	}
}

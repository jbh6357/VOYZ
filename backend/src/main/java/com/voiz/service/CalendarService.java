package com.voiz.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.voiz.mapper.CalendarRepository;
import com.voiz.mapper.MarketingRepository;
import com.voiz.vo.Marketing;

@Service
public class CalendarService {

	@Autowired
	private MarketingRepository marketingRepository;

	@Autowired
    private CalendarRepository calendarRepository; 
	
	public Marketing getMarketing(int marketingIdx) {
		Optional<Marketing> marketing = marketingRepository.findByMarketingIdx(marketingIdx);
		if(marketing.isPresent()) {
			return marketing.get();
		}else {
			return null;
		}
	}

	// 사용자 ID로 마케팅 제안 목록을 가져오는 로직
    public List<Marketing> getMarketingProposalsByUserId(String userId) {
        return calendarRepository.findMarketingByUserId(userId);
    }

}
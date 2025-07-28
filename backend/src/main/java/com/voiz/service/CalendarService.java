package com.voiz.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.voiz.mapper.MarketingRepository;
import com.voiz.vo.Marketing;

@Service
public class CalendarService {

	@Autowired
	private MarketingRepository marketingRepository;
	
	public Marketing getMarketing(int marketingIdx) {
		Optional<Marketing> marketing = marketingRepository.findByMarketingIdx(marketingIdx);
		if(marketing.isPresent()) {
			return marketing.get();
		}else {
			return null;
		}
	}

}

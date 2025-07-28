package com.voiz.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.voiz.dto.ReminderDto;
import com.voiz.mapper.MarketingRepository;
import com.voiz.mapper.ReminderRepository;
import com.voiz.vo.Marketing;

@Service
public class CalendarService {

	@Autowired
	private MarketingRepository marketingRepository;
	
	@Autowired
	private ReminderRepository reminderRepository;
	
	public Marketing getMarketing(int marketingIdx) {
		Optional<Marketing> marketing = marketingRepository.findByMarketingIdx(marketingIdx);
		if(marketing.isPresent()) {
			return marketing.get();
		}else {
			return null;
		}
	}

	public void createReminder(ReminderDto reminderDto, String userId) {
		int reminderIdx = reminderRepository.findReminderIdxByUserId(userId);
		Marketing marketing = new Marketing();
		marketing.setContent(reminderDto.getContent());
		marketing.setTitle(reminderDto.getTitle());
		marketing.setStartDate(reminderDto.getStartDate());
		marketing.setEndDate(reminderDto.getEndDate());
		marketing.setReminder_idx(reminderIdx);
		marketing.setStatus("진행전");
		marketing.setType("1");
		marketingRepository.save(marketing);
	}
}

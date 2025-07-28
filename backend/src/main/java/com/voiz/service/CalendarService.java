package com.voiz.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.voiz.dto.ReminderDto;
import com.voiz.mapper.MarketingRepository;
import com.voiz.mapper.ReminderRepository;
import com.voiz.mapper.SpecialDaySuggestRepository;
import com.voiz.vo.Marketing;
import com.voiz.vo.SpecialDaySuggest;

@Service
public class CalendarService {

	@Autowired
	private MarketingRepository marketingRepository;
	
	@Autowired
	private ReminderRepository reminderRepository;
	
	@Autowired
	private SpecialDaySuggestRepository specialDaySuggestRepository;
	
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

	public List<Marketing> getMarketingListByUserAndMonth(String userId, int year, int month) {
		int reminderIdx = reminderRepository.findReminderIdxByUserId(userId);
		
		// 해당 월 기준 전월 ~ 다음월 범위 계산
	    YearMonth ym = YearMonth.of(year, month);
	    LocalDate from = ym.minusMonths(1).atDay(1);          // 전월 1일
	    LocalDate to = ym.plusMonths(1).atEndOfMonth();       // 다음월 말일

	    return marketingRepository.findByReminderIdxAndDateRange(reminderIdx, from, to);
	}

	public SpecialDaySuggest getSpecialDaySuggestion(int ssuIdx) {
		Optional<SpecialDaySuggest> suggestion = specialDaySuggestRepository.findBySsuIdx(ssuIdx);
		if(suggestion.isPresent()) {
			return suggestion.get();
		}else {
			return null;
		}
	}
}

package com.voiz.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.voiz.dto.DaySuggestionDto;
import com.voiz.dto.ReminderDto;
import com.voiz.mapper.CalendarRepository;
import com.voiz.mapper.MarketingRepository;
import com.voiz.mapper.ReminderRepository;
import com.voiz.mapper.SpecialDayRepository;
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
	
	@Autowired
	private SpecialDayRepository specialDayRepository;
	
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

	public void createReminder(ReminderDto reminderDto, String userId) {
		System.out.println("=== createReminder ===");
		System.out.println("userId: " + userId);
		System.out.println("title: " + reminderDto.getTitle());
		System.out.println("content: " + reminderDto.getContent());
		System.out.println("startDate: " + reminderDto.getStartDate());
		System.out.println("endDate: " + reminderDto.getEndDate());
		
		int reminderIdx = reminderRepository.findReminderIdxByUserId(userId);
		System.out.println("reminderIdx: " + reminderIdx);
		
		Marketing marketing = new Marketing();
		marketing.setContent(reminderDto.getContent());
		marketing.setTitle(reminderDto.getTitle());
		marketing.setStartDate(reminderDto.getStartDate());
		marketing.setEndDate(reminderDto.getEndDate());
		marketing.setReminder_idx(reminderIdx);
		marketing.setStatus("진행전");
		marketing.setType("1");
		
		System.out.println("Before save - startDate: " + marketing.getStartDate() + ", endDate: " + marketing.getEndDate());
		marketingRepository.save(marketing);
		System.out.println("After save - marketingIdx: " + marketing.getMarketingIdx());
		System.out.println("=== End createReminder ===");
	}

	public List<Marketing> getMarketingListByUserAndMonth(String userId, int year, int month) {
		System.out.println("=== getMarketingListByUserAndMonth ===");
		System.out.println("userId: " + userId + ", year: " + year + ", month: " + month);
		
		int reminderIdx = reminderRepository.findReminderIdxByUserId(userId);
		System.out.println("reminderIdx: " + reminderIdx);
		
		// 해당 월 기준 전월 ~ 다음월 범위 계산
	    YearMonth ym = YearMonth.of(year, month);
	    LocalDate from = ym.minusMonths(1).atDay(1);          // 전월 1일
	    LocalDate to = ym.plusMonths(1).atEndOfMonth();       // 다음월 말일
	    System.out.println("Date range: " + from + " ~ " + to);

	    List<Marketing> result = marketingRepository.findByReminderIdxAndDateRange(reminderIdx, from, to);
	    System.out.println("Query result count: " + result.size());
	    for (Marketing m : result) {
	    	System.out.println("Marketing: " + m.getMarketingIdx() + ", " + m.getTitle() + ", " + m.getStartDate() + "~" + m.getEndDate());
	    }
	    System.out.println("=== End getMarketingListByUserAndMonth ===");
	    
	    return result;
	}

	public SpecialDaySuggest getSpecialDaySuggestion(int ssuIdx) {
		Optional<SpecialDaySuggest> suggestion = specialDaySuggestRepository.findBySsuIdx(ssuIdx);
		if(suggestion.isPresent()) {
			return suggestion.get();
		}else {
			return null;
		}
	}

	public List<DaySuggestionDto> getDaySuggestionsByUserAndMonth(String userId, int year, int month) {
		int calendarIdx = calendarRepository.findCalendarIdxByUserId(userId);
		
		// 해당 월 기준 전월 ~ 다음월 범위 계산
	    YearMonth ym = YearMonth.of(year, month);
	    LocalDate from = ym.minusMonths(1).atDay(1);          // 전월 1일
	    LocalDate to = ym.plusMonths(1).atEndOfMonth();       // 다음월 말일
		
	    List<DaySuggestionDto> daySuggestionList = specialDayRepository.findSpecialDaysWithSuggestion(calendarIdx, from, to);
		return daySuggestionList;
	}
}

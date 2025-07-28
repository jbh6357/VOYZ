package com.voiz.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.voiz.dto.ReminderDto;
import com.voiz.service.CalendarService;
import com.voiz.vo.Marketing;
import com.voiz.vo.SpecialDaySuggest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/calendars")
@Tag(name = "calendar", description = "일정관리 API")
public class CalendarController {
	
	@Autowired
	private CalendarService calendarService;
	
	@GetMapping("/reminder/{marketing_idx}")
	@Operation(summary = "리마인더 일정 상세보기", description = "사용자가 특정 리마인더를 클릭했을 때 해당 일정 상세보기를 제공하기 위함입니다.")
	public ResponseEntity<Marketing> getReminderByMarketingIdx(@PathVariable int marketing_idx) {
	    Marketing reminder = calendarService.getMarketing(marketing_idx);
	    
	    if (reminder != null) {
	        return ResponseEntity.ok(reminder);
	    } else {
	        return ResponseEntity.notFound().build(); 
	    }
	}
	
	@PostMapping("/reminder")
	@Operation(summary = "리마인더 등록", description = "사용자가 새로운 리마인더 일정을 등록할 때 사용하는 API입니다.")
	public ResponseEntity<Void> createReminder(@RequestBody ReminderDto reminderDto, @RequestParam String userId) {
	    calendarService.createReminder(reminderDto, userId);
	    return ResponseEntity.ok().build(); 
	}
	
	@GetMapping("/reminder")
	@Operation(summary = "리마인더 일정 조회", description = "사용자의 리마인더 ID를 기반으로 마케팅 일정을 월 기준으로 조회합니다. (전월~다음월까지)")
	public ResponseEntity<List<Marketing>> getRemindersByUserAndMonth(
	        @RequestParam("user_id") String userId,
	        @RequestParam int year,
	        @RequestParam int month) {

	    List<Marketing> marketingList = calendarService.getMarketingListByUserAndMonth(userId, year, month);
	    return ResponseEntity.ok(marketingList);
	}
	
	@GetMapping("/day-sug/{ssu_idx}")
	@Operation(summary = "특일 제안 단건 조회", description = "특정 제안(ssu_idx)을 조회합니다.")
	public ResponseEntity<SpecialDaySuggest> getSpecialDaySuggestion(@PathVariable("ssu_idx") int ssuIdx) {
	    SpecialDaySuggest suggestion = calendarService.getSpecialDaySuggestion(ssuIdx);
	    return ResponseEntity.ok(suggestion);
	}
	
	
}

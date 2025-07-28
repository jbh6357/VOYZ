package com.voiz.controller;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/calendars")
@Tag(name = "calendar-controller", description = "일정관리 API")
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
	
	
}

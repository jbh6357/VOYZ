package com.voiz.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

     // 특일 제안(마케팅) 조회 API
    @GetMapping("/proposals/{userId}")
    @Operation(summary = "사용자의 마케팅 제안 조회", description = "특정 사용자의 캘린더에 표시될 모든 마케팅 제안 목록을 조회합니다.")
    public ResponseEntity<List<Marketing>> getMarketingProposalsByUserId(@PathVariable String userId) {
        List<Marketing> proposals = calendarService.getMarketingProposalsByUserId(userId);
        if (proposals == null || proposals.isEmpty()) {
            return ResponseEntity.noContent().build(); // 데이터가 없으면 204 No Content
        }
        return ResponseEntity.ok(proposals);
    }

}
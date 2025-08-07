package com.voiz.controller;

import com.voiz.dto.MenuSalesDto;
import com.voiz.dto.SalesAnalyticsDto;
import com.voiz.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate; // LocalDate 임포트
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "analytics", description = "통계 분석 API")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/sales/{userId}")
    @Operation(summary = "매출 통계 조회", description = "시작일과 종료일을 기준으로 매출 통계를 조회합니다. 조회 기간에 따라 집계 단위(월/주/요일)가 자동으로 변경됩니다.")
    public ResponseEntity<List<SalesAnalyticsDto>> getSalesAnalytics(
            @PathVariable String userId,
            @RequestParam LocalDate startDate, // 'yyyy-MM-dd' 형식
            @RequestParam LocalDate endDate) {

        List<SalesAnalyticsDto> analytics = analyticsService.getSalesAnalytics(userId, startDate, endDate);
        return ResponseEntity.ok(analytics);
    }



    @GetMapping("/menus/{userId}/popular")
    @Operation(summary = "메뉴별 판매량 TOP 5 조회", description = "지정된 기간 동안 가장 많이 팔린 메뉴 5개와 그 비중을 조회합니다.")
    public ResponseEntity<List<MenuSalesDto>> getTopMenuSales(
            @PathVariable String userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        
        List<MenuSalesDto> topMenus = analyticsService.getTopMenuSales(userId, startDate, endDate);
        return ResponseEntity.ok(topMenus);
    }
}
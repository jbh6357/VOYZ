package com.voiz.controller;

import com.voiz.dto.MenuSalesDto;
import com.voiz.dto.NationalityAnalyticsDto;
import com.voiz.dto.ReviewSummaryDto;
import com.voiz.dto.OrderTimeAnalyticsDto;
import com.voiz.dto.SalesAnalyticsDto;
import com.voiz.dto.CountryRatingDto;
import com.voiz.dto.MenuSentimentDto;
import com.voiz.service.AnalyticsService;
import com.voiz.service.FastApiClient;
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
    private final FastApiClient fastApiClient;


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
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) String category, // 카테고리 필터링 (선택적)
            @RequestParam(defaultValue = "5") int topCount) { // 기본값은 5개
        
        List<MenuSalesDto> topMenus = analyticsService.getTopMenuSales(userId, startDate, endDate, category, topCount);
        return ResponseEntity.ok(topMenus);
    }




    @GetMapping("/customers/{userId}/nationality")
    @Operation(summary = "국적별 리뷰 통계 조회", description = "특정 사용자의 리뷰를 국적별로 집계하여 반환합니다. 기간(year, month, week) 필터링이 가능합니다.")
    public ResponseEntity<List<NationalityAnalyticsDto>> getNationalityStats(
            @PathVariable String userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer week) {
        List<NationalityAnalyticsDto> stats = analyticsService.getNationalityAnalytics(userId, year, month, week);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/customers/{userId}/nationality/summary")
    @Operation(summary = "국적 통계 요약(내/외국인)", description = "국적 분포와 함께 내국인/외국인 수를 요약하여 반환합니다.")
    public ResponseEntity<com.voiz.dto.NationalitySummaryDto> getNationalitySummary(
            @PathVariable String userId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer week) {
        var summary = analyticsService.getNationalitySummary(userId, year, month, week);
        return ResponseEntity.ok(summary);
    }



    @GetMapping("/orders/{userId}/time")
    @Operation(summary = "시간대별 주문 통계 조회", description = "지정된 기간 동안의 시간대별 주문 건수를 조회하여, 가게의 피크 타임 분석 데이터를 제공합니다.")
    public ResponseEntity<List<OrderTimeAnalyticsDto>> getOrderAnalyticsByTime(
            @PathVariable String userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        List<OrderTimeAnalyticsDto> analytics = analyticsService.getOrderAnalyticsByTime(userId, startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/reviews/{userId}/summary")
    @Operation(summary = "리뷰 요약 통계", description = "기간 기준 총 리뷰 수, 평균 평점, 긍정/부정 리뷰 수를 반환합니다. 긍/부정 임계값은 기본값(positive>=4, negative<=2)을 사용합니다.")
    public ResponseEntity<ReviewSummaryDto> getReviewSummary(
            @PathVariable String userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "4") int positiveThreshold,
            @RequestParam(defaultValue = "2") int negativeThreshold
    ) {
        ReviewSummaryDto summary = analyticsService.getReviewSummary(userId, startDate, endDate, positiveThreshold, negativeThreshold);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/reviews/{userId}")
    @Operation(summary = "리뷰 목록 조회", description = "기간/국적/평점/메뉴 필터로 리뷰 목록을 조회합니다.")
    public ResponseEntity<List<com.voiz.dto.ReviewResponseDto>> getReviews(
            @PathVariable String userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) String nationality,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Integer maxRating,
            @RequestParam(required = false) List<Integer> menuIds
    ) {
        var reviews = analyticsService.getReviewsByFilters(userId, startDate, endDate, nationality, minRating, maxRating, menuIds);
        // 메뉴명 매핑
        java.util.Set<Integer> menuIdSet = reviews.stream().map(com.voiz.vo.Reviews::getMenuIdx).collect(java.util.stream.Collectors.toSet());
        java.util.Map<Integer, String> menuIdToName = analyticsService.getMenuNames(menuIdSet);
        // map to DTO
        List<com.voiz.dto.ReviewResponseDto> dtos = reviews.stream().map(r -> {
            var dto = new com.voiz.dto.ReviewResponseDto();
            dto.setReviewIdx(r.getReviewIdx().intValue());
            dto.setMenuIdx(r.getMenuIdx());
            dto.setOrderIdx(r.getOrderIdx());
            dto.setUserId(r.getUserId());
            dto.setComment(r.getComment());
            dto.setRating(r.getRating());
            dto.setNationality(r.getNationality());
            dto.setLanguage(r.getLanguage());
            dto.setCreatedAt(r.getCreatedAt());
            dto.setMenuName(menuIdToName.get(r.getMenuIdx()));
            return dto;
        }).toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/reviews/{userId}/keywords")
    @Operation(summary = "리뷰 키워드 분석", description = "긍/부정 상위 키워드를 전체 및 메뉴별로 반환합니다.")
    public ResponseEntity<String> getReviewKeywords(
            @PathVariable String userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "4") int positiveThreshold,
            @RequestParam(defaultValue = "2") int negativeThreshold,
            @RequestParam(defaultValue = "5") int topK,
            @RequestParam(defaultValue = "openai") String mode
    ) {
        var reviews = analyticsService.getReviewsByFilters(userId, startDate, endDate, null, null, null, null);

        // ML 서버 payload 구성
        java.util.List<java.util.Map<String, Object>> comments = new java.util.ArrayList<>();
        for (var r : reviews) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("text", r.getComment());
            m.put("rating", r.getRating());
            m.put("menuIdx", r.getMenuIdx());
            m.put("language", r.getLanguage());
            comments.add(m);
        }
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("comments", comments);
        payload.put("positiveThreshold", positiveThreshold);
        payload.put("negativeThreshold", negativeThreshold);
        payload.put("topK", topK);
        payload.put("mode", mode);

        var response = fastApiClient.postDataToFastApi("/api/reviews/keywords", payload);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    @GetMapping("/reviews/{userId}/nationality-ratings")
    @Operation(summary = "국가별 평균 평점 및 작성 수", description = "기간 내 국가별 리뷰 수와 평균 평점을 집계합니다.")
    public ResponseEntity<java.util.List<com.voiz.dto.CountryRatingDto>> getCountryRatings(
            @PathVariable String userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        var list = analyticsService.getCountryRatings(userId, startDate, endDate);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/reviews/{userId}/menu-sentiment")
    @Operation(summary = "메뉴별 긍/부정 집계 및 평균 평점", description = "기간 내 메뉴별 긍정/부정/중립 개수와 평균 평점을 집계합니다.")
    public ResponseEntity<java.util.List<com.voiz.dto.MenuSentimentDto>> getMenuSentiment(
            @PathVariable String userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "4") int positiveThreshold,
            @RequestParam(defaultValue = "2") int negativeThreshold
    ) {
        var list = analyticsService.getMenuSentiment(userId, startDate, endDate, positiveThreshold, negativeThreshold);
        return ResponseEntity.ok(list);
    }
}
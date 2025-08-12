package com.voiz.controller;

import com.voiz.dto.MenuSalesDto;
import com.voiz.dto.NationalityAnalyticsDto;
import com.voiz.dto.ReviewSummaryDto;
import com.voiz.dto.OrderTimeAnalyticsDto;
import com.voiz.dto.SalesAnalyticsDto;
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

    @GetMapping("/sales/{userId}/hourly")
    @Operation(summary = "시간대별 매출액 합계 조회", description = "지정된 기간 동안 00~23시 각 시간대의 총 매출액을 반환합니다.")
    public ResponseEntity<List<OrderTimeAnalyticsDto>> getSalesAmountByHour(
            @PathVariable String userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        List<OrderTimeAnalyticsDto> list = analyticsService.getSalesAmountByHour(userId, startDate, endDate);
        return ResponseEntity.ok(list);
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
            dto.setGuestName(r.getGuestName());
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
            @RequestParam(defaultValue = "2") int negativeThreshold,
            @RequestParam(required = false) String nationality,
            @RequestParam(defaultValue = "false") boolean includeSummary
    ) {
        System.out.println("📊 Controller: 메뉴 감정 분석 API 호출");
        System.out.println("  - userId: " + userId);
        System.out.println("  - startDate: " + startDate);
        System.out.println("  - endDate: " + endDate);
        System.out.println("  - nationality: " + nationality);
        System.out.println("  - includeSummary: " + includeSummary);
        
        java.util.List<com.voiz.dto.MenuSentimentDto> list;
        
        if (includeSummary) {
            System.out.println("🔄 Controller: 한줄평 포함 요청 - getMenuSentimentWithSummary 호출");
            list = analyticsService.getMenuSentimentWithSummary(userId, startDate, endDate, positiveThreshold, negativeThreshold, nationality);
        } else {
            System.out.println("🔄 Controller: 기본 요청 - getMenuSentiment 호출");
            list = analyticsService.getMenuSentiment(userId, startDate, endDate, positiveThreshold, negativeThreshold, nationality);
        }
        
        System.out.println("✅ Controller: 메뉴 감정 분석 완료, 결과 수: " + list.size());
        list.forEach(menu -> {
            System.out.println("  🍽️ " + menu.getMenuName() + " - 한줄평: " + menu.getReviewSummary());
        });
        
        return ResponseEntity.ok(list);
    }

    @GetMapping("/reviews/{userId}/nationalities")
    @Operation(summary = "리뷰 작성 국가 목록", description = "해당 매장에 리뷰를 작성한 국가 목록을 조회합니다.")
    public ResponseEntity<java.util.List<String>> getReviewNationalities(
            @PathVariable String userId
    ) {
        var list = analyticsService.getReviewNationalities(userId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/reviews/{userId}/insights")
    @Operation(summary = "메뉴별 리뷰 인사이트", description = "메뉴별 감정 분석 결과를 바탕으로 사장님용 인사이트를 생성합니다.")
    public ResponseEntity<java.util.Map<String, Object>> getMenuInsights(
            @PathVariable String userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "4") int positiveThreshold,
            @RequestParam(defaultValue = "2") int negativeThreshold,
            @RequestParam(required = false) String nationality
    ) {
        var menus = analyticsService.getMenuSentimentWithSummary(userId, startDate, endDate, positiveThreshold, negativeThreshold, nationality);
        var insights = analyticsService.generateMenuInsights(menus);
        return ResponseEntity.ok(insights);
    }

    @GetMapping("/sales/{userId}/insights")
    @Operation(summary = "매출 인사이트", description = "매출 데이터를 분석하여 AI 기반 인사이트를 생성합니다.")
    public ResponseEntity<java.util.Map<String, Object>> getSalesInsights(
            @PathVariable String userId,
            @RequestParam(defaultValue = "month") String period
    ) {
        return ResponseEntity.ok(analyticsService.getSalesInsights(userId, period));
    }

    @GetMapping("/reviews/{userId}/comprehensive-insights")
    @Operation(summary = "종합 리뷰 인사이트", description = "전체 리뷰를 분석하여 핵심 인사이트 3가지를 생성합니다.")
    public ResponseEntity<java.util.Map<String, Object>> getComprehensiveInsights(
            @PathVariable String userId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        // 기본값 설정: 최근 1개월
        if (startDate == null || endDate == null) {
            LocalDate today = LocalDate.now();
            startDate = today.minusMonths(1).withDayOfMonth(1);
            endDate = today;
        }
        
        var insights = analyticsService.generateComprehensiveInsights(userId, startDate, endDate);
        return ResponseEntity.ok(insights);
    }

    @GetMapping("/period-insights/{userId}")
    @Operation(summary = "AI 기반 기간별 운영 인사이트", description = "ML 서비스를 활용하여 매출 예측, 고객 패턴, 메뉴 추천 등 종합적인 운영 인사이트를 제공합니다.")
    public ResponseEntity<java.util.Map<String, Object>> getPeriodInsights(
            @PathVariable String userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "month") String period
    ) {
        try {
            System.out.println("🔍 기간별 인사이트 API 호출: " + userId + " (" + startDate + " ~ " + endDate + ")");
            
            // 1. 매출 데이터 수집
            var salesData = analyticsService.getSalesAnalytics(userId, startDate, endDate);
            System.out.println("📊 매출 데이터 수집 완료: " + salesData.size() + "건");
            
            // 2. 메뉴 데이터 수집
            var menuData = analyticsService.getTopMenuSales(userId, startDate, endDate, null, 10);
            System.out.println("🍽️ 메뉴 데이터 수집 완료: " + menuData.size() + "건");
            
            // 3. 고객 데이터 수집
            var customerData = analyticsService.getNationalityAnalytics(userId, null, null, null);
            System.out.println("👥 고객 데이터 수집 완료: " + customerData.size() + "건");
            
            // 4. 이전 기간 비교 데이터 (같은 기간만큼 이전)
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
            LocalDate prevStartDate = startDate.minusDays(daysBetween + 1);
            LocalDate prevEndDate = startDate.minusDays(1);
            var previousSalesData = analyticsService.getSalesAnalytics(userId, prevStartDate, prevEndDate);
            System.out.println("📈 이전 기간 데이터 수집 완료: " + previousSalesData.size() + "건");
            
            // 5. ML 서비스로 전송할 데이터 구성
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("salesData", salesData);
            payload.put("menuData", menuData);
            payload.put("customerData", customerData);
            payload.put("period", period);
            payload.put("previousPeriodData", java.util.Map.of("sales", previousSalesData));
            
            // 6. ML 서비스 호출
            System.out.println("🤖 ML 서비스 호출 중...");
            var mlResponse = fastApiClient.postDataToFastApi("/api/analytics/period-insights", payload);
            
            if (mlResponse.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ ML 인사이트 생성 완료");
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.Map<String, Object> insights = mapper.readValue(mlResponse.getBody(), java.util.Map.class);
                    
                    // 추가 메타데이터 포함
                    insights.put("metadata", java.util.Map.of(
                        "period", period,
                        "startDate", startDate.toString(),
                        "endDate", endDate.toString(),
                        "dataPoints", salesData.size(),
                        "generatedAt", java.time.LocalDateTime.now().toString()
                    ));
                    
                    return ResponseEntity.ok(insights);
                } catch (Exception e) {
                    System.err.println("❌ ML 응답 파싱 오류: " + e.getMessage());
                    return ResponseEntity.ok(java.util.Map.of(
                        "error", "AI 분석 중 오류가 발생했습니다",
                        "rawResponse", mlResponse.getBody()
                    ));
                }
            } else {
                System.err.println("❌ ML 서비스 오류: " + mlResponse.getStatusCode());
                return ResponseEntity.status(mlResponse.getStatusCode())
                    .body(java.util.Map.of("error", "AI 분석 서비스에 연결할 수 없습니다"));
            }
            
        } catch (Exception e) {
            System.err.println("❌ 기간별 인사이트 생성 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of(
                "error", "인사이트 생성 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/customer-behavior/{userId}")
    @Operation(summary = "AI 기반 고객 행동 패턴 분석", description = "ML 서비스를 활용하여 고객의 주문 패턴, 국가별 선호도, 시간대별 트렌드를 분석합니다.")
    public ResponseEntity<java.util.Map<String, Object>> getCustomerBehaviorAnalysis(
            @PathVariable String userId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "month") String period
    ) {
        try {
            System.out.println("🕵️ 고객 행동 분석 API 호출: " + userId);
            
            // 1. 주문 이력 데이터 수집
            var orderAnalytics = analyticsService.getOrderAnalyticsByTime(userId, startDate, endDate);
            System.out.println("📋 주문 데이터 수집 완료: " + orderAnalytics.size() + "건");
            
            // 2. 리뷰 데이터 수집
            var reviews = analyticsService.getReviewsByFilters(userId, startDate, endDate, null, null, null, null);
            System.out.println("⭐ 리뷰 데이터 수집 완료: " + reviews.size() + "건");
            
            // 3. ML 서비스용 데이터 변환
            java.util.List<java.util.Map<String, Object>> orderHistory = new java.util.ArrayList<>();
            for (var order : orderAnalytics) {
                java.util.Map<String, Object> orderMap = new java.util.HashMap<>();
                orderMap.put("time", order.getHour() + ":00");
                orderMap.put("orderCount", order.getOrderCount());
                orderMap.put("amount", order.getOrderCount() * 15000); // 평균 주문금액 가정
                orderHistory.add(orderMap);
            }
            
            java.util.List<java.util.Map<String, Object>> reviewHistory = new java.util.ArrayList<>();
            for (var review : reviews.stream().limit(50).toList()) { // 최근 50건만
                java.util.Map<String, Object> reviewMap = new java.util.HashMap<>();
                reviewMap.put("nationality", review.getNationality());
                reviewMap.put("rating", review.getRating());
                reviewMap.put("menuId", review.getMenuIdx());
                reviewMap.put("comment", review.getComment());
                reviewHistory.add(reviewMap);
            }
            
            // 4. ML 서비스 호출
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("orderHistory", orderHistory);
            payload.put("reviewHistory", reviewHistory);
            payload.put("period", period);
            
            System.out.println("🤖 고객 행동 분석 ML 서비스 호출 중...");
            var mlResponse = fastApiClient.postDataToFastApi("/api/analytics/customer-behavior", payload);
            
            if (mlResponse.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ 고객 행동 분석 완료");
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.Map<String, Object> analysis = mapper.readValue(mlResponse.getBody(), java.util.Map.class);
                    
                    // 메타데이터 추가
                    analysis.put("metadata", java.util.Map.of(
                        "period", period,
                        "orderDataPoints", orderHistory.size(),
                        "reviewDataPoints", reviewHistory.size(),
                        "analysisDate", java.time.LocalDateTime.now().toString()
                    ));
                    
                    return ResponseEntity.ok(analysis);
                } catch (Exception e) {
                    System.err.println("❌ 고객 행동 분석 파싱 오류: " + e.getMessage());
                    return ResponseEntity.ok(java.util.Map.of(
                        "error", "고객 행동 분석 중 오류가 발생했습니다",
                        "rawResponse", mlResponse.getBody()
                    ));
                }
            } else {
                System.err.println("❌ 고객 행동 분석 ML 서비스 오류: " + mlResponse.getStatusCode());
                return ResponseEntity.status(mlResponse.getStatusCode())
                    .body(java.util.Map.of("error", "고객 행동 분석 서비스에 연결할 수 없습니다"));
            }
            
        } catch (Exception e) {
            System.err.println("❌ 고객 행동 분석 오류: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(java.util.Map.of(
                "error", "고객 행동 분석 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
}
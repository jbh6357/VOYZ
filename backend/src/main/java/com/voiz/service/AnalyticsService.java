package com.voiz.service;

import com.voiz.dto.MenuSalesDto;
import com.voiz.dto.NationalityAnalyticsDto;
import com.voiz.dto.ReviewSummaryDto;
import com.voiz.dto.OrderTimeAnalyticsDto;
import com.voiz.dto.SalesAnalyticsDto;
import com.voiz.dto.CountryRatingDto;
import com.voiz.dto.MenuSentimentDto;
import com.voiz.mapper.ReviewRepository;
import com.voiz.mapper.SalesOrderRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit; // 날짜 계산을 위해 임포트
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final SalesOrderRepository salesOrderRepository;
    
    private final ReviewRepository reviewRepository;
    
    private final com.voiz.mapper.MenusRepository menusRepository;


    // Controller가 호출하는 메서드.
    public List<SalesAnalyticsDto> getSalesAnalytics(String userId, LocalDate startDate, LocalDate endDate) {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 두 날짜 사이의 간격을 계산하여 조회 단위를 자동으로 결정합니다.
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);

        String groupBy;
        if (daysBetween > 122) {      // 122일(4달) 이상 차이나면 '월' 단위로
            groupBy = "month";
        } else if (daysBetween > 7) { // 7일 초과면 '주' 단위로
            groupBy = "week";
        } else {                      // 그 외에는 '요일' 단위로
            groupBy = "dayOfWeek";
        }

        List<Object[]> results;
        switch (groupBy) {
            case "month":
                results = salesOrderRepository.findSalesByPeriodGroupedByMonth(userId, startDateTime, endDateTime);
                break;
            case "week":
                results = salesOrderRepository.findSalesByPeriodGroupedByWeek(userId, startDateTime, endDateTime);
                break;
            default: // "dayOfWeek"
                results = salesOrderRepository.findSalesByPeriodGroupedByDayOfWeek(userId, startDateTime, endDateTime);
                break;
        }

        // DB 결과를 DTO로 변환하는 공통 로직
        return results.stream()
                .map(record -> {
                    String timeGroup = String.valueOf(record[0]);
                    BigDecimal totalSalesDecimal = (BigDecimal) record[1];
                    Double totalSales = (totalSalesDecimal != null) ? totalSalesDecimal.doubleValue() : 0.0;
                    return new SalesAnalyticsDto(timeGroup, totalSales);
                })
                .collect(Collectors.toList());
    }









    // TOP 5 메뉴 관련 메서드
     public List<MenuSalesDto> getTopMenuSales(String userId, LocalDate startDate, LocalDate endDate, String category, int topCount) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        
        List<Object[]> topSalesData = salesOrderRepository.findTopSellingMenus(userId, startDateTime, endDateTime, category, topCount);

        
        if (topSalesData == null || topSalesData.isEmpty()) {
            return new ArrayList<>();
        }

        double grandTotal = topSalesData.stream()
            .mapToDouble(record -> ((BigDecimal) record[1]).doubleValue())
            .sum();

        return topSalesData.stream()
            .map(record -> {
                String menuName = (String) record[0];
                double itemSales = ((BigDecimal) record[1]).doubleValue();
                double percentage = (grandTotal > 0) ? (itemSales / grandTotal) * 100 : 0;
                double roundedPercentage = Math.round(percentage * 10.0) / 10.0;
                
                return new MenuSalesDto(menuName, roundedPercentage);
            })
            .collect(Collectors.toList());
    }


    public List<NationalityAnalyticsDto> getNationalityAnalytics(String userId, Integer year, Integer month, Integer week) {
        
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        if (year != null) {
            startDateTime = LocalDateTime.of(year, 1, 1, 0, 0);
            endDateTime = LocalDateTime.of(year + 1, 1, 1, 0, 0);
        } else if (month != null) {
            startDateTime = LocalDateTime.of(LocalDate.now().getYear(), month, 1, 0, 0);
            endDateTime = startDateTime.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
        } else if (week != null) {
            LocalDate startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).plusWeeks(week - 1);
            startDateTime = startOfWeek.atStartOfDay();
            endDateTime = startOfWeek.plusDays(6).atTime(LocalTime.MAX);
        } else {
            // 디폴트 갓은 최근 한 달로 설정
            LocalDate today = LocalDate.now();
            startDateTime = today.minusMonths(1).atStartOfDay();
            endDateTime = today.atTime(LocalTime.MAX);
        }

        return reviewRepository.countReviewsByNationalityAndDateRange(userId, startDateTime, endDateTime);

        
    }

    public com.voiz.dto.NationalitySummaryDto getNationalitySummary(String userId, Integer year, Integer month, Integer week) {
        var list = getNationalityAnalytics(userId, year, month, week);
        long local = list.stream()
                .filter(dto -> isLocal(dto.getNationality()))
                .mapToLong(NationalityAnalyticsDto::getCount)
                .sum();
        long total = list.stream().mapToLong(NationalityAnalyticsDto::getCount).sum();
        long foreign = total - local;
        return new com.voiz.dto.NationalitySummaryDto(local, foreign, list);
    }

    private boolean isLocal(String nationality) {
        if (nationality == null) return false;
        String n = nationality.trim().toUpperCase();
        return n.equals("KR") || n.equals("KOR") || n.equals("KOREA") || n.equals("대한민국") || n.equals("한국");
    }

    public ReviewSummaryDto getReviewSummary(String userId, LocalDate startDate, LocalDate endDate, int positiveThreshold, int negativeThreshold) {
        var startDateTime = startDate.atStartOfDay();
        var endDateTime = endDate.atTime(LocalTime.MAX);

        Object[] summary = reviewRepository.summarizeReviews(userId, startDateTime, endDateTime);
        long total = 0L;
        double avg = 0.0;
        if (summary != null && summary.length >= 2) {
            Number totalNum = (Number) summary[0];
            Number avgNum = (Number) summary[1];
            total = totalNum != null ? totalNum.longValue() : 0L;
            avg = avgNum != null ? avgNum.doubleValue() : 0.0;
        }

        long positive = reviewRepository.countPositive(userId, positiveThreshold, startDateTime, endDateTime);
        long negative = reviewRepository.countNegative(userId, negativeThreshold, startDateTime, endDateTime);

        return new ReviewSummaryDto(total, avg, positive, negative);
    }

    public List<com.voiz.vo.Reviews> getReviewsByFilters(
            String userId,
            LocalDate startDate,
            LocalDate endDate,
            String nationality,
            Integer minRating,
            Integer maxRating,
            List<Integer> menuIds
    ) {
        var startDateTime = startDate.atStartOfDay();
        var endDateTime = endDate.atTime(LocalTime.MAX);
        return reviewRepository.findReviewsByFilters(userId, startDateTime, endDateTime, nationality, minRating, maxRating, menuIds);
    }

    public java.util.Map<Integer, String> getMenuNames(java.util.Set<Integer> menuIdxSet) {
        if (menuIdxSet == null || menuIdxSet.isEmpty()) return java.util.Collections.emptyMap();
        var menus = menusRepository.findAllById(menuIdxSet);
        java.util.Map<Integer, String> map = new java.util.HashMap<>();
        for (var m : menus) {
            map.put(m.getMenuIdx(), m.getMenuName());
        }
        return map;
    }



    // 시간별 주문 통계 조회 메서드
    public List<OrderTimeAnalyticsDto> getOrderAnalyticsByTime(String userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Object[]> results = salesOrderRepository.findOrderCountByHour(userId, startDateTime, endDateTime);

        return results.stream()
                .map((Object[] record) -> { 
                    String hour = (String) record[0];
                    Long orderCount = ((Number) record[1]).longValue();
                    return new OrderTimeAnalyticsDto(hour, orderCount);
                })
                .collect(Collectors.toList());
    }

    public java.util.List<com.voiz.dto.CountryRatingDto> getCountryRatings(
            String userId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        var startDateTime = startDate.atStartOfDay();
        var endDateTime = endDate.atTime(LocalTime.MAX);
        var rows = reviewRepository.aggregateCountryRatings(userId, startDateTime, endDateTime);
        java.util.List<com.voiz.dto.CountryRatingDto> list = new java.util.ArrayList<>();
        for (Object[] r : rows) {
            String nationality = (String) r[0];
            long count = ((Number) r[1]).longValue();
            double avg = ((Number) r[2]).doubleValue();
            list.add(new com.voiz.dto.CountryRatingDto(nationality, count, avg));
        }
        return list;
    }

    public java.util.List<com.voiz.dto.MenuSentimentDto> getMenuSentiment(
            String userId,
            LocalDate startDate,
            LocalDate endDate,
            int positiveThreshold,
            int negativeThreshold,
            String nationality
    ) {
        var startDateTime = startDate.atStartOfDay();
        var endDateTime = endDate.atTime(LocalTime.MAX);
        var rows = reviewRepository.aggregateMenuSentiment(userId, startDateTime, endDateTime, positiveThreshold, negativeThreshold, nationality);
        java.util.Set<Integer> menuIds = new java.util.HashSet<>();
        for (Object[] r : rows) {
            menuIds.add(((Number) r[0]).intValue());
        }
        var idToName = getMenuNames(menuIds);
        java.util.List<com.voiz.dto.MenuSentimentDto> list = new java.util.ArrayList<>();
        for (Object[] r : rows) {
            int menuId = ((Number) r[0]).intValue();
            long count = ((Number) r[1]).longValue();
            long pos = ((Number) r[2]).longValue();
            long neg = ((Number) r[3]).longValue();
            double avg = ((Number) r[4]).doubleValue();
            long neutral = count - pos - neg;
            String menuName = idToName.get(menuId);
            list.add(new com.voiz.dto.MenuSentimentDto(menuId, menuName, pos, neg, neutral, avg));
        }
        return list;
    }

    public java.util.List<String> getReviewNationalities(String userId) {
        return reviewRepository.findDistinctNationalitiesByUserId(userId);
    }

    public String generateMenuReviewSummary(Integer menuId, String menuName, String userId, String nationality) {
        // 해당 메뉴의 리뷰 데이터 조회
        var reviews = reviewRepository.findReviewsByMenuAndUser(menuId, userId, nationality);
        
        if (reviews.isEmpty()) {
            return "리뷰가 없습니다";
        }
        
        // 감정별 개수 계산
        long positiveCount = reviews.stream().mapToLong(r -> ((Number) r[1]).intValue() >= 4 ? 1 : 0).sum();
        long neutralCount = reviews.stream().mapToLong(r -> ((Number) r[1]).intValue() == 3 ? 1 : 0).sum();
        long negativeCount = reviews.stream().mapToLong(r -> ((Number) r[1]).intValue() <= 2 ? 1 : 0).sum();
        
        try {
            // 가장 많은 비중을 차지하는 감정 결정
            String prioritySentiment = "positive";
            if (negativeCount > positiveCount && negativeCount > neutralCount) {
                prioritySentiment = "negative";
            } else if (neutralCount > positiveCount && neutralCount > negativeCount) {
                prioritySentiment = "neutral";
            }
            
            // ML 서비스 요청 데이터 구성 (내용 분석용)
            java.util.List<java.util.Map<String, Object>> reviewList = new java.util.ArrayList<>();
            for (Object[] review : reviews) {
                String text = (String) review[0];
                int rating = ((Number) review[1]).intValue();
                
                String sentiment = "neutral";
                if (rating >= 4) sentiment = "positive";
                else if (rating <= 2) sentiment = "negative";
                
                java.util.Map<String, Object> reviewData = new java.util.HashMap<>();
                reviewData.put("text", text);
                reviewData.put("sentiment", sentiment);
                reviewData.put("rating", rating);
                reviewList.add(reviewData);
            }
            
            // ML 서비스 호출 (내용 분석)
            RestTemplate restTemplate = new RestTemplate();
            String mlServiceUrl = "http://localhost:8000/api/reviews/content-analysis";
            
            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("menuName", menuName);
            requestBody.put("reviews", reviewList);
            requestBody.put("prioritySentiment", prioritySentiment);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<java.util.Map> response = restTemplate.postForEntity(mlServiceUrl, entity, java.util.Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("insight");
            }
            
        } catch (Exception e) {
            System.err.println("ML 서비스 호출 실패: " + e.getMessage());
        }
        
        // ML 서비스 실패시 감정별 기본 메시지 생성 (다수 의견 기준)
        if (positiveCount > negativeCount && positiveCount > neutralCount) {
            return "맛있다고 해요";
        } else if (negativeCount > positiveCount && negativeCount > neutralCount) {
            return "개선이 필요해요";
        } else if (neutralCount > positiveCount && neutralCount > negativeCount) {
            return "괜찮은 편이에요";
        } else {
            return "의견이 다양해요";
        }
    }

    public java.util.List<com.voiz.dto.MenuSentimentDto> getMenuSentimentWithSummary(
            String userId,
            LocalDate startDate,
            LocalDate endDate,
            int positiveThreshold,
            int negativeThreshold,
            String nationality
    ) {
        var list = getMenuSentiment(userId, startDate, endDate, positiveThreshold, negativeThreshold, nationality);
        
        // 각 메뉴에 대해 한줄 평 생성
        for (MenuSentimentDto menu : list) {
            String summary = generateMenuReviewSummary(menu.getMenuId(), menu.getMenuName(), userId, nationality);
            menu.setReviewSummary(summary);
        }
        
        return list;
    }

    public java.util.Map<String, Object> generateComprehensiveInsights(String userId, LocalDate startDate, LocalDate endDate) {
        try {
            // 전체 리뷰 데이터 조회 (메뉴 이름 포함)
            var startDateTime = startDate.atStartOfDay();
            var endDateTime = endDate.atTime(java.time.LocalTime.MAX);
            var reviewsWithMenus = reviewRepository.findReviewsWithMenuName(userId, startDateTime, endDateTime, null, null, null, null);
            
            if (reviewsWithMenus.isEmpty()) {
                java.util.Map<String, Object> emptyResult = new java.util.HashMap<>();
                java.util.List<java.util.Map<String, Object>> emptyInsights = new java.util.ArrayList<>();
                emptyInsights.add(java.util.Map.of(
                    "type", "trend",
                    "title", "데이터 부족",
                    "description", "더 많은 리뷰가 필요해요",
                    "priority", "low"
                ));
                emptyResult.put("insights", emptyInsights);
                return emptyResult;
            }
            
            // ML 서비스 요청 데이터 구성
            java.util.List<java.util.Map<String, Object>> reviewList = new java.util.ArrayList<>();
            for (Object[] reviewData : reviewsWithMenus) {
                // Object[] 구조: reviewIdx, orderIdx, menuIdx, userId, comment, rating, nationality, language, createdAt, menuName
                java.util.Map<String, Object> reviewMap = new java.util.HashMap<>();
                reviewMap.put("comment", reviewData[4]);  // comment
                reviewMap.put("rating", reviewData[5]);   // rating
                reviewMap.put("nationality", reviewData[6]); // nationality
                reviewMap.put("menuName", reviewData[9]); // menuName
                reviewMap.put("createdAt", reviewData[8].toString()); // createdAt
                reviewList.add(reviewMap);
            }
            
            // 기간 타입 결정
            long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
            String timeRange = daysBetween <= 7 ? "week" : daysBetween <= 31 ? "month" : "year";
            
            // ML 서비스 호출
            RestTemplate restTemplate = new RestTemplate();
            String mlServiceUrl = "http://localhost:8000/api/reviews/comprehensive-insights";
            
            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("reviews", reviewList);
            requestBody.put("timeRange", timeRange);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<java.util.Map> response = restTemplate.postForEntity(mlServiceUrl, entity, java.util.Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            System.err.println("종합 인사이트 생성 실패: " + e.getMessage());
        }
        
        // 기본 인사이트 반환
        java.util.Map<String, Object> defaultResult = new java.util.HashMap<>();
        java.util.List<java.util.Map<String, Object>> defaultInsights = new java.util.ArrayList<>();
        
        defaultInsights.add(java.util.Map.of(
            "type", "trend",
            "title", "리뷰 활동 증가",
            "description", "고객들의 관심이 높아졌어요",
            "priority", "high"
        ));
        
        defaultInsights.add(java.util.Map.of(
            "type", "improvement",
            "title", "개선 기회 발견",
            "description", "고객 의견을 분석해보세요",
            "priority", "medium"
        ));
        
        defaultInsights.add(java.util.Map.of(
            "type", "strength",
            "title", "긍정 평가 유지",
            "description", "전반적으로 좋은 평가예요",
            "priority", "high"
        ));
        
        defaultResult.put("insights", defaultInsights);
        return defaultResult;
    }

    public java.util.Map<String, Object> generateMenuInsights(java.util.List<MenuSentimentDto> menus) {
        try {
            // ML 서비스 요청 데이터 구성
            java.util.List<java.util.Map<String, Object>> menuList = new java.util.ArrayList<>();
            for (MenuSentimentDto menu : menus) {
                java.util.Map<String, Object> menuData = new java.util.HashMap<>();
                menuData.put("menuName", menu.getMenuName());
                menuData.put("positiveCount", menu.getPositiveCount());
                menuData.put("negativeCount", menu.getNegativeCount());
                menuData.put("neutralCount", menu.getNeutralCount());
                menuData.put("averageRating", menu.getAverageRating());
                menuData.put("reviewSummary", menu.getReviewSummary());
                menuList.add(menuData);
            }
            
            // ML 서비스 호출
            RestTemplate restTemplate = new RestTemplate();
            String mlServiceUrl = "http://localhost:8000/api/reviews/insights";
            
            java.util.Map<String, Object> requestBody = new java.util.HashMap<>();
            requestBody.put("menus", menuList);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<java.util.Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            ResponseEntity<java.util.Map> response = restTemplate.postForEntity(mlServiceUrl, entity, java.util.Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            System.err.println("인사이트 생성 실패: " + e.getMessage());
        }
        
        // 기본 인사이트 반환
        java.util.Map<String, Object> defaultInsights = new java.util.HashMap<>();
        java.util.List<String> insights = new java.util.ArrayList<>();
        java.util.List<String> recommendations = new java.util.ArrayList<>();
        
        if (!menus.isEmpty()) {
            long totalReviews = menus.stream().mapToLong(m -> 
                m.getPositiveCount() + m.getNegativeCount() + m.getNeutralCount()).sum();
            insights.add("총 " + totalReviews + "개의 리뷰를 분석했습니다");
            recommendations.add("지속적으로 고객 피드백을 확인해보세요");
        }
        
        defaultInsights.put("insights", insights);
        defaultInsights.put("recommendations", recommendations);
        return defaultInsights;
    }


}
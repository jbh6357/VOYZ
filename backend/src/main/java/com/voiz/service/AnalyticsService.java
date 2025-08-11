package com.voiz.service;

import com.voiz.dto.MenuSalesDto;
import com.voiz.dto.NationalityAnalyticsDto;
import com.voiz.dto.OrderTimeAnalyticsDto;
import com.voiz.dto.SalesAnalyticsDto;
import com.voiz.mapper.ReviewRepository;
import com.voiz.mapper.SalesOrderRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


}
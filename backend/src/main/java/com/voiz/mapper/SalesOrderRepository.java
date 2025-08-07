package com.voiz.mapper;

import com.voiz.vo.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

        // 1. 연도별 조회 (월 단위)
        @Query(value = "SELECT TO_CHAR(o.created_at, 'YYYY-MM'), SUM(o.total_amount) " +
                        "FROM VOYZ_ORDERS o " +
                        "WHERE o.user_id = :userId AND o.status = 'Completed' " +
                        "  AND o.created_at BETWEEN :startDate AND :endDate " +
                        "GROUP BY TO_CHAR(o.created_at, 'YYYY-MM') " +
                        "ORDER BY 1", nativeQuery = true)
        List<Object[]> findSalesByPeriodGroupedByMonth(@Param("userId") String userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // 월별 조회 (주 단위)
        @Query(value = "SELECT TO_CHAR(o.created_at, 'W'), SUM(o.total_amount) " +
                        "FROM VOYZ_ORDERS o " +
                        "WHERE o.user_id = :userId AND o.status = 'Completed' " +
                        "  AND o.created_at BETWEEN :startDate AND :endDate " +
                        "GROUP BY TO_CHAR(o.created_at, 'W') " +
                        "ORDER BY 1", nativeQuery = true)
        List<Object[]> findSalesByPeriodGroupedByWeek(@Param("userId") String userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // 주별 조회 (요일 단위)
        @Query(value = "SELECT TO_CHAR(o.created_at, 'D'), SUM(o.total_amount) " +
                        "FROM VOYZ_ORDERS o " +
                        "WHERE o.user_id = :userId AND o.status = 'Completed' " +
                        "  AND o.created_at BETWEEN :startDate AND :endDate " +
                        "GROUP BY TO_CHAR(o.created_at, 'D') " +
                        "ORDER BY 1", nativeQuery = true)
        List<Object[]> findSalesByPeriodGroupedByDayOfWeek(@Param("userId") String userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        
        

        // 메뉴별 매출 상위 5개 조회
        @Query(value = "SELECT MENU_NAME, TOTAL_SALES FROM ( " +
                   "  SELECT m.MENU_NAME, SUM(oi.TOTAL_PRICE) as TOTAL_SALES " +
                   "  FROM VOYZ_ORDERS o " +
                   "  JOIN VOYZ_ORDERS_ITEMS oi ON o.ORDER_IDX = oi.ORDER_IDX " +
                   "  JOIN VOYZ_MENUS m ON oi.MENU_IDX = m.MENU_IDX " +
                   "  WHERE o.USER_ID = :userId " +
                   "    AND o.STATUS = 'Completed' " +
                   "    AND o.CREATED_AT BETWEEN :startDate AND :endDate " +
                   "    AND (:category IS NULL OR m.CATEGORY = :category) " + 
                   "  GROUP BY m.MENU_NAME " +
                   "  ORDER BY TOTAL_SALES DESC " +
                   ") WHERE ROWNUM <= :topCount", 
           nativeQuery = true)
    List<Object[]> findTopSellingMenus(@Param("userId") String userId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       @Param("category") String category, 
                                       @Param("topCount") int topCount);  

}
package com.voiz.mapper;

import com.voiz.dto.NationalityAnalyticsDto;
import com.voiz.vo.Reviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Reviews, Long> {

    
    @Query("SELECT r FROM Reviews r WHERE r.menuIdx = :menuId")
    List<Reviews> findByMenuIdx(@Param("menuId") int menuId);


    @Query("SELECT new com.voiz.dto.NationalityAnalyticsDto(r.nationality, COUNT(r)) " +
           "FROM Reviews r WHERE r.userId = :userId AND r.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY r.nationality " +
           "ORDER BY COUNT(r) DESC")
    List<NationalityAnalyticsDto> countReviewsByNationalityAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(r), AVG(r.rating) FROM Reviews r WHERE r.userId = :userId AND r.createdAt BETWEEN :startDate AND :endDate")
    Object[] summarizeReviews(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(r) FROM Reviews r WHERE r.userId = :userId AND r.rating >= :positiveThreshold AND r.createdAt BETWEEN :startDate AND :endDate")
    long countPositive(
            @Param("userId") String userId,
            @Param("positiveThreshold") int positiveThreshold,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(r) FROM Reviews r WHERE r.userId = :userId AND r.rating <= :negativeThreshold AND r.createdAt BETWEEN :startDate AND :endDate")
    long countNegative(
            @Param("userId") String userId,
            @Param("negativeThreshold") int negativeThreshold,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM Reviews r WHERE r.userId = :userId AND r.createdAt BETWEEN :startDate AND :endDate " +
            "AND (:nationality IS NULL OR r.nationality = :nationality) " +
            "AND (:minRating IS NULL OR r.rating >= :minRating) " +
            "AND (:maxRating IS NULL OR r.rating <= :maxRating) " +
            "AND (:menuIds IS NULL OR r.menuIdx IN :menuIds) " +
            "ORDER BY r.createdAt DESC")
    List<Reviews> findReviewsByFilters(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("nationality") String nationality,
            @Param("minRating") Integer minRating,
            @Param("maxRating") Integer maxRating,
            @Param("menuIds") List<Integer> menuIds);

    @Query("SELECT r.nationality, COUNT(r), AVG(r.rating) " +
           "FROM Reviews r WHERE r.userId = :userId AND r.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY r.nationality " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> aggregateCountryRatings(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r.menuIdx, COUNT(r), " +
           "SUM(CASE WHEN r.rating >= :positiveThreshold THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.rating <= :negativeThreshold THEN 1 ELSE 0 END), " +
           "AVG(r.rating) " +
           "FROM Reviews r WHERE r.userId = :userId AND r.createdAt BETWEEN :startDate AND :endDate " +
           "AND (:nationality IS NULL OR r.nationality = :nationality) " +
           "GROUP BY r.menuIdx " +
           "HAVING COUNT(r) > 0 " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> aggregateMenuSentiment(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("positiveThreshold") int positiveThreshold,
            @Param("negativeThreshold") int negativeThreshold,
            @Param("nationality") String nationality);

    @Query("SELECT DISTINCT r.nationality FROM Reviews r WHERE r.userId = :userId ORDER BY r.nationality")
    List<String> findDistinctNationalitiesByUserId(@Param("userId") String userId);

}
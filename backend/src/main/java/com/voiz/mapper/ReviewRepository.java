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

}
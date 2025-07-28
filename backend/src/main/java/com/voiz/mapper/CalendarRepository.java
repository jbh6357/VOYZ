package com.voiz.mapper;

import com.voiz.vo.Calendars;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CalendarRepository extends JpaRepository<Calendars, Long> {

    // JPQL을 사용하여 Calendars와 Marketing 엔티티를 조인하여 바로 Marketing 목록을 가져옴
    @Query("SELECT c.marketing FROM Calendars c WHERE c.user.userId = :userId")
    List<com.voiz.vo.Marketing> findMarketingByUserId(String userId);
}
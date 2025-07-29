package com.voiz.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.voiz.vo.Marketing;

@Repository
public interface MarketingRepository extends JpaRepository<Marketing, Integer> {
	Optional<Marketing> findByMarketingIdx(int marketingIdx);

	@Query("SELECT m FROM Marketing m WHERE m.reminder_idx = :reminderIdx AND (m.startDate <= :to AND m.endDate >= :from)")
	List<Marketing> findByReminderIdxAndDateRange(@Param("reminderIdx") int reminderIdx,
	                                              @Param("from") LocalDate from,
	                                              @Param("to") LocalDate to);
}

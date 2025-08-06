package com.voiz.mapper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.voiz.vo.Calendar;

@Repository
public interface CalendarRepository extends JpaRepository<Calendar, Integer>{

	@Query("SELECT c.calendarIdx FROM Calendar c WHERE c.userId = :userId")
	int findCalendarIdxByUserId(@Param("userId") String userId);

}

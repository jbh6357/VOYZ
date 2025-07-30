package com.voiz.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.voiz.dto.DaySuggestionDto;
import com.voiz.dto.MatchSpecialDayDto;
import com.voiz.vo.SpecialDay;

@Repository
public interface SpecialDayRepository extends JpaRepository<SpecialDay, Integer> {
	
	@Query("SELECT new com.voiz.dto.MatchSpecialDayDto(s.sdIdx, s.name, s.category) FROM SpecialDay s")
	List<MatchSpecialDayDto> findForMatch();
	
	@Query("""
		    SELECT new com.voiz.dto.DaySuggestionDto(d, ds)
		    FROM SpecialDay d
		    LEFT JOIN SpecialDayMatch dm ON d.sdIdx = dm.sd_idx
		    LEFT JOIN SpecialDaySuggest ds ON dm.sm_idx = ds.sm_idx
		    AND ds.calendarIdx = :calendarIdx
		    WHERE d.startDate BETWEEN :from AND :to
		""")
		List<DaySuggestionDto> findSpecialDaysWithSuggestion(
		    @Param("calendarIdx") int calendarIdx,
		    @Param("from") LocalDate from,
		    @Param("to") LocalDate to
		);
}

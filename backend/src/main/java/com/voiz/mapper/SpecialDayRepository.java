package com.voiz.mapper;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.voiz.dto.MatchSpecialDayDto;
import com.voiz.vo.SpecialDay;

@Repository
public interface SpecialDayRepository extends JpaRepository<SpecialDay, Integer> {
	
	@Query("SELECT new com.voiz.dto.MatchSpecialDayDto(s.sd_idx, s.name, s.category) FROM SpecialDay s")
	List<MatchSpecialDayDto> findForMatch();
}

package com.voiz.mapper;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.voiz.vo.SpecialDaySuggest;

@Repository
public interface SpecialDaySuggestRepository extends JpaRepository<SpecialDaySuggest, Integer>{

	Optional<SpecialDaySuggest> findBySsuIdx(int ssuIdx);
	
	@Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM SpecialDaySuggest s WHERE s.smIdx = :smIdx")
	boolean existsBySmIdx(@Param("smIdx") int smIdx);

}

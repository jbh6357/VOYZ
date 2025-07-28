package com.voiz.mapper;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.voiz.vo.SpecialDaySuggest;

@Repository
public interface SpecialDaySuggestRepository extends JpaRepository<SpecialDaySuggest, Integer>{

	Optional<SpecialDaySuggest> findBySsuIdx(int ssuIdx);

}

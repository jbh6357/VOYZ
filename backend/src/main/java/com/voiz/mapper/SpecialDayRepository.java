package com.voiz.mapper;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.voiz.vo.SpecialDay;

@Repository
public interface SpecialDayRepository extends JpaRepository<SpecialDay, Integer> {

}

package com.voiz.mapper;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.voiz.vo.Marketing;

@Repository
public interface MarketingRepository extends JpaRepository<Marketing, Integer> {
	Optional<Marketing> findByMarketingIdx(int marketingIdx);
}

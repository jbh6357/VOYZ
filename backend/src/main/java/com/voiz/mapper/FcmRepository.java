package com.voiz.mapper;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.voiz.vo.Fcm;

@Repository
public interface FcmRepository extends JpaRepository<Fcm, Integer>{

	Optional<Fcm> findByUserId(String userId);
	
}

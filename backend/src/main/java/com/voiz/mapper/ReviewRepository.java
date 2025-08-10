package com.voiz.mapper;

import com.voiz.vo.Reviews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Reviews, Long> {

    
    @Query("SELECT r FROM Reviews r WHERE r.menuIdx = :menuIdx")
    List<Reviews> findByMenuIdx(@Param("menuIdx") long menuIdx);
    
}
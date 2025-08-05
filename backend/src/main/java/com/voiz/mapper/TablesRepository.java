package com.voiz.mapper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.voiz.vo.Tables;

@Repository
public interface TablesRepository extends JpaRepository<Tables, Integer> {
	@Query("SELECT MAX(t.tableNumber) FROM Tables t WHERE t.userId = :userId")
	Integer findMaxTableNumberByUserId(@Param("userId") String userId);
}

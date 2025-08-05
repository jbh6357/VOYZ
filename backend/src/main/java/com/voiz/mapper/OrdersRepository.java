package com.voiz.mapper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.voiz.vo.Orders;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Integer>{
	@Query(
		    value = "SELECT MAX(ORDER_NUMBER) FROM VOYZ_ORDERS WHERE USER_ID = :userId AND TRUNC(CREATED_AT) = TRUNC(SYSDATE)",
		    nativeQuery = true
		)
		Integer findTodayMaxOrderNumberByUserId(@Param("userId") String userId);
}

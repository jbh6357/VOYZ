package com.voiz.mapper;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.voiz.vo.OrdersItems;

@Repository
public interface OrdersItemsRepository extends JpaRepository<OrdersItems, Integer>{

	List<OrdersItems> findAllByOrderIdx(int orderIdx);

	void deleteByOrderIdx(int orderIdx);

}

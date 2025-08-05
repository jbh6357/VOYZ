package com.voiz.mapper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.voiz.vo.OrdersItems;

@Repository
public interface OrdersItemsRepository extends JpaRepository<OrdersItems, Integer>{

}

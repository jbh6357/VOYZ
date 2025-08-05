package com.voiz.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.voiz.dto.OrderRequestDto;
import com.voiz.service.OrderService;
import com.voiz.vo.OrdersItems;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "orders", description = "주문 및 주문상세 관리 API")
public class OrderController {
	
	@Autowired
	private OrderService orderService;
	
	@PostMapping("/")
    @Operation(summary = "주문 생성", description = "주문과 주문 상세를 생성합니다.")
    public ResponseEntity<String> createOrder(@RequestBody OrderRequestDto requestDto) {
        orderService.createOrder(requestDto);
        return ResponseEntity.ok("주문이 성공적으로 생성되었습니다.");
    }
	
	@GetMapping("/{orderIdx}")
    @Operation(summary = "주문 상세 조회", description = "주문번호에 해당하는 주문 상세 정보를 가져옵니다.")
    public ResponseEntity<List<OrdersItems>> getOrderItems(@PathVariable int orderIdx) {
        List<OrdersItems> items = orderService.getOrderItems(orderIdx);
        return ResponseEntity.ok(items);
    }
	
	
	
	
}

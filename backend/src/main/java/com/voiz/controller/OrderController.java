package com.voiz.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.voiz.dto.OrderRequestDto;
import com.voiz.service.OrderService;
import com.voiz.vo.Orders;
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
	
	@PutMapping("/{orderIdx}")
	@Operation(summary = "주문 수정", description = "주문번호에 해당하는 주문 정보를 수정합니다.")
	public ResponseEntity<String> createOrder2(
			@PathVariable int orderIdx,
			@RequestBody OrderRequestDto requestDto) {
        orderService.updateOrder(orderIdx, requestDto);
        return ResponseEntity.ok("주문이 성공적으로 수정되었습니다.");
    }
	
	@GetMapping("/restaurant/{userId}")
	@Operation(summary = "매장별 주문 목록 조회", description = "userId, status, date(yyyy-mm-dd)에 해당하는 주문 목록을 조회합니다. date가 없으면 오늘 날짜로 조회합니다.")
	public ResponseEntity<List<Orders>> getOrdersByUserIdAndStatusAndDate(
	        @PathVariable String userId,
	        @RequestParam String status,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
	    if (date == null) {
	        date = LocalDate.now(); // 오늘 날짜로 대체
	    }

	    List<Orders> orders = orderService.getOrdersByUserIdAndStatusAndDate(userId, status, date);
	    return ResponseEntity.ok(orders);
	}
	
	@PutMapping("/{orderIdx}/status")
	@Operation(summary = "주문 상태 변경", description = "주문번호에 해당하는 주문의 상태를 변경합니다.")
	public ResponseEntity<String> updateOrderStatus(@PathVariable int orderIdx, @RequestParam String status) {
	   orderService.updateOrderStatus(orderIdx, status);
	   return ResponseEntity.ok("주문 상태가 성공적으로 변경되었습니다.");
	}
}

package com.voiz.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.voiz.dto.MenusDto;
import com.voiz.service.OrderService;
import com.voiz.vo.Menus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/customer")
@Tag(name = "customer", description = "고객 주문관리 API")
public class CustomerController {
	
	@Autowired
	private OrderService orderService;
	
	@GetMapping("/menu/{userId}")
	@Operation(summary = "고객용 메뉴판 조회", description = "고객이 QR코드를 스캔했을 때 언어에 맞는 메뉴판을 조회하기 위한 API입니다.")
	public ResponseEntity<List<MenusDto>> getCustomerMenus(
			@PathVariable String userId, 
			@RequestParam String language) {
		
		List<MenusDto> menus= orderService.getCustomerMenus(userId, language);
		return ResponseEntity.ok(menus);
	}
	
	@GetMapping("/menu/detail/{menuIdx}")
	@Operation(summary = "고객용 메뉴 상세 조회", description = "메뉴 하나에 대한 상세 정보를 언어에 맞게 반환합니다.")
	public ResponseEntity<Menus> getCustomerMenuDetail(
	        @PathVariable int menuIdx,
	        @RequestParam String language) {

	    Menus menu = orderService.getCustomerMenuDetail(menuIdx, language);
	    return ResponseEntity.ok(menu);
	}
	
}

package com.voiz.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.voiz.dto.MenusDto;
import com.voiz.dto.OrderRequestDto;
import com.voiz.mapper.MenusRepository;
import com.voiz.mapper.OrdersItemsRepository;
import com.voiz.mapper.OrdersRepository;
import com.voiz.vo.Menus;
import com.voiz.vo.OrdersItems;
import com.voiz.vo.Orders;

@Service
public class OrderService {

	@Autowired
	private MenusRepository menusRepository;
	
	@Autowired
	private OrdersRepository ordersRepository;
	
	@Autowired
	private OrdersItemsRepository ordersItemsRepository;
	
	@Autowired
	private FastApiClient fastApiClient;
	
	public List<MenusDto> getCustomerMenus(String userId, String language) {
		List<Menus> menus = menusRepository.findAllByUserId(userId);
		return menus.stream()
	            .map(menu -> {
	                // 번역 (한국어면 번역 안 함)
	                String translatedName = fastApiClient.requestTranslate(menu.getMenuName(), language);
	                String translatedDesc = fastApiClient.requestTranslate(menu.getMenuDescription(), language);

	                return new MenusDto(
	                		menu.getMenuIdx(),
	                        translatedName,
	                        menu.getMenuPrice(),
	                        translatedDesc,
	                        menu.getImageUrl()
	                );
	            })
	            .toList();
	}

	public Menus getCustomerMenuDetail(int menuIdx, String language) {
		Optional<Menus> optionalMenu = menusRepository.findById(menuIdx);
		
		if(optionalMenu.isEmpty()) {
			throw new RuntimeException("잘못된 메뉴Idx입니다.");
		}
		
		Menus menu = optionalMenu.get();
		
		menu.setMenuName(fastApiClient.requestTranslate(menu.getMenuName(), language));
		menu.setMenuDescription(fastApiClient.requestTranslate(menu.getMenuDescription(), language));
		
		return menu;
	}

	@Transactional
	public void createOrder(OrderRequestDto dto) {
		
		// 1. 오늘 주문 중 가장 큰 주문번호 가져오기
        Integer maxOrderNumber = ordersRepository.findTodayMaxOrderNumberByUserId(dto.getUserId());
        if (maxOrderNumber == null) {
        	maxOrderNumber = 0;
        }
        
        // 2. 주문 정보 생성
        Orders order = new Orders();
        order.setUserId(dto.getUserId());
        order.setTableIdx(dto.getTableIdx());
        order.setOrderNumber(String.valueOf(maxOrderNumber + 1));
        order.setTotalAmount("0"); // 총액 계산 전임
        order.setSpecialRequests(dto.getSpecialRequest());
        order.setStatus("주문완료");
        order.setOrderLanguage(dto.getOrderLanguage());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        ordersRepository.save(order);
        
        // 3. 주문 상세 정보 입력
        int totalAmount = 0;
        for (OrderRequestDto.OrderDetailRequest detail : dto.getOrderDetails()) {
            // 단가, 총액은 실제 비즈니스 로직에 맞게 추출해야 함
        	
        	Optional<Menus> optionalMenu = menusRepository.findById(detail.getMenuIdx());
            if(optionalMenu.isEmpty()) {
            	throw new RuntimeException("잘못된 메뉴Idx입니다.");
            }
        	Menus menu = optionalMenu.get();
        	int unitPrice = menu.getMenuPrice(); 
            int totalPrice = unitPrice * detail.getQuantity();
            totalAmount += totalPrice;

            OrdersItems item = new OrdersItems();
            item.setOrderIdx(order.getOrderIdx());
            item.setMenuIdx(detail.getMenuIdx());
            item.setQuantity(detail.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setTotalPrice(totalPrice);
            item.setItemOptions(detail.getMenuOption());
            item.setSpecialRequests(detail.getSpecialRequest());

            ordersItemsRepository.save(item);
        }
        
        // 3. 총액 업데이트
        order.setTotalAmount(String.valueOf(totalAmount));
        ordersRepository.save(order);
	}

	public List<OrdersItems> getOrderItems(int orderIdx) {
		return ordersItemsRepository.findAllByOrderIdx(orderIdx);
	}
	

}

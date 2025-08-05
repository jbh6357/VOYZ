package com.voiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.voiz.dto.MenusDto;
import com.voiz.mapper.MenusRepository;
import com.voiz.vo.Menus;

@Service
public class OrderService {

	@Autowired
	private MenusRepository menusRepository;
	
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
	                        translatedName,
	                        menu.getMenuPrice(),
	                        translatedDesc,
	                        menu.getImageUrl()
	                );
	            })
	            .toList();
	}

}

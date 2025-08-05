package com.voiz.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.voiz.mapper.MenusRepository;
import com.voiz.vo.Menus;

@Service
public class MenuService {

	@Autowired
	private FastApiClient fastApiClient;
	
	@Autowired
	private MenusRepository menusRepository;
	
	public String sendToMlServer(MultipartFile file) throws IOException {
		
		ResponseEntity<String> response =  fastApiClient.requestOcr(file);
		
		return response.getBody();
	}

	public String sendToMlServer(String menuName, String targetLanguage) {

		ResponseEntity<String> response = fastApiClient.requestTranslate(menuName, targetLanguage);
		
		return response.getBody();
	}

	public void createMenu(String userId, String menuName, int menuPrice) {
		Menus menu = new Menus();
	    menu.setUserId(userId);
	    menu.setMenuName(menuName);
	    menu.setMenuPrice(menuPrice);
	    menusRepository.save(menu);
	}

	public String uploadMenuImage(int menuIdx, MultipartFile file) throws IOException {
		// 1. 메뉴 존재 여부 확인
		Optional<Menus> optionalMenu = menusRepository.findById(menuIdx);
		if (optionalMenu.isEmpty()) {
		    throw new IllegalArgumentException("해당 메뉴가 존재하지 않습니다. menuIdx = " + menuIdx);
		}
		   
		Menus menu = optionalMenu.get();
		   
		try {
		   // 프로젝트 루트 기준으로 이미지 파일 저장 경로 설정
		   String uploadsDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "menuImages" + File.separator;
		       
		   // 파일 이름 생성
		   String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + file.getOriginalFilename();
		       
		   // 실제 파일이 저장될 경로
		   String filePath = uploadsDir + fileName;
		       
		   // DB에 저장할 경로 문자열 (웹에서 접근 가능한 경로)
		   String dbFilePath = "/uploads/menuImages/" + fileName;
		       
		   // 디렉토리 생성
		   File dir = new File(uploadsDir);
		   if (!dir.exists()) {
		       dir.mkdirs();
		   }
		       
		   // 파일 저장
		   File savedFile = new File(dir, fileName);
		   file.transferTo(savedFile);
		       
		   // DB 업데이트
		   menu.setImageUrl(dbFilePath);
		   menu.setUpdatedAt(LocalDateTime.now());
		   menusRepository.save(menu);
		       
		   return dbFilePath;
		       
		} catch (IOException e) {
		   e.printStackTrace();
		}	
		return null;
	}

	public void deleteMenu(int menuIdx) {
		Optional<Menus> optionalMenu = menusRepository.findById(menuIdx);
	    if (optionalMenu.isEmpty()) {
	        throw new IllegalArgumentException("해당 메뉴가 존재하지 않습니다. menuIdx = " + menuIdx);
	    }
	    menusRepository.deleteById(menuIdx);
		
	}

	public List<Menus> getMenusByUserId(String userId) {
		return menusRepository.findAllByUserId(userId);	
	}

	public void updateMenu(int menuIdx, String menuName, int menuPrice) {
		Optional<Menus> optionalMenu = menusRepository.findById(menuIdx);
	    if (optionalMenu.isEmpty()) {
	        throw new IllegalArgumentException("해당 메뉴가 존재하지 않습니다. menuIdx = " + menuIdx);
	    }

	    Menus menu = optionalMenu.get();
	    menu.setMenuName(menuName);
	    menu.setMenuPrice(menuPrice);
	    menu.setUpdatedAt(LocalDateTime.now());

	    menusRepository.save(menu);
	}
}
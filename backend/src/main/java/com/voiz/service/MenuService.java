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

		String response = fastApiClient.requestTranslate(menuName, targetLanguage);
		
		return response;
	}

	public void createMenu(String userId, String menuName, int menuPrice, String menuDescription, String category) {
		// 중복 메뉴 체크 (같은 사용자, 같은 메뉴명)
		List<Menus> existingMenus = menusRepository.findByUserIdAndMenuName(userId, menuName);
		if (!existingMenus.isEmpty()) {
			throw new IllegalArgumentException("이미 등록된 메뉴입니다: " + menuName);
		}
		
		Menus menu = new Menus();
	    menu.setUserId(userId);
	    menu.setMenuName(menuName);
	    menu.setMenuPrice(menuPrice);
	    menu.setMenuDescription(menuDescription);
	    menu.setCategory(category);
	    menusRepository.save(menu);
	}
	
	public void createMenuWithImage(String userId, String menuName, int menuPrice, String menuDescription, 
			String category, MultipartFile image) throws IOException {
		// 중복 메뉴 체크 (같은 사용자, 같은 메뉴명)
		List<Menus> existingMenus = menusRepository.findByUserIdAndMenuName(userId, menuName);
		if (!existingMenus.isEmpty()) {
			throw new IllegalArgumentException("이미 등록된 메뉴입니다: " + menuName);
		}
		
		Menus menu = new Menus();
		menu.setUserId(userId);
		menu.setMenuName(menuName);
		menu.setMenuPrice(menuPrice);
		menu.setMenuDescription(menuDescription);
		menu.setCategory(category);
		
		// 이미지가 있으면 저장
		if (image != null && !image.isEmpty()) {
			// 프로젝트 루트 기준으로 이미지 파일 저장 경로 설정
			String uploadsDir = System.getProperty("user.dir") + File.separator + "uploads" + 
					File.separator + "menuImages" + File.separator;
			
			// 파일 이름 생성
			String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + image.getOriginalFilename();
			
			// DB에 저장할 경로 문자열 (웹에서 접근 가능한 경로)
			String dbFilePath = "/uploads/menuImages/" + fileName;
			
			// 디렉토리 생성
			File dir = new File(uploadsDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			
			// 파일 저장
			File savedFile = new File(dir, fileName);
			image.transferTo(savedFile);
			
			// 이미지 URL 설정
			menu.setImageUrl(dbFilePath);
		}
		
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

	public void updateMenu(int menuIdx, String menuName, int menuPrice, String menuDescription, String category) {
		Optional<Menus> optionalMenu = menusRepository.findById(menuIdx);
	    if (optionalMenu.isEmpty()) {
	        throw new IllegalArgumentException("해당 메뉴가 존재하지 않습니다. menuIdx = " + menuIdx);
	    }

	    Menus menu = optionalMenu.get();
	    menu.setMenuName(menuName);
	    menu.setMenuPrice(menuPrice);
	    menu.setMenuDescription(menuDescription);
	    menu.setCategory(category);
	    menu.setUpdatedAt(LocalDateTime.now());

	    menusRepository.save(menu);
	}
	
	public void updateMenuWithImage(int menuIdx, String menuName, int menuPrice, String menuDescription, String category, MultipartFile image) throws IOException {
		Optional<Menus> optionalMenu = menusRepository.findById(menuIdx);
	    if (optionalMenu.isEmpty()) {
	        throw new IllegalArgumentException("해당 메뉴가 존재하지 않습니다. menuIdx = " + menuIdx);
	    }

	    Menus menu = optionalMenu.get();
	    
	    // 새 이미지가 있으면 기존 이미지 삭제 후 새 이미지 저장
	    if (image != null && !image.isEmpty()) {
	        // 기존 이미지 파일 삭제
	        if (menu.getImageUrl() != null && !menu.getImageUrl().isEmpty()) {
	            String oldImagePath = menu.getImageUrl();
	            String fullPath = System.getProperty("user.dir") + File.separator + 
	                (oldImagePath.startsWith("/") ? oldImagePath.substring(1).replace("/", File.separator) : oldImagePath);
	            File oldImageFile = new File(fullPath);
	            if (oldImageFile.exists()) {
	                oldImageFile.delete();
	            }
	        }
	        
	        // 새 이미지 저장 (createMenuWithImage와 동일한 로직)
	        String uploadsDir = System.getProperty("user.dir") + File.separator + "uploads" + 
	            File.separator + "menuImages" + File.separator;
	        
	        String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + image.getOriginalFilename();
	        String dbFilePath = "/uploads/menuImages/" + fileName;
	        
	        File dir = new File(uploadsDir);
	        if (!dir.exists()) {
	            dir.mkdirs();
	        }
	        
	        File savedFile = new File(dir, fileName);
	        image.transferTo(savedFile);
	        
	        menu.setImageUrl(dbFilePath);
	    }
	    
	    // 메뉴 정보 업데이트
	    menu.setMenuName(menuName);
	    menu.setMenuPrice(menuPrice);
	    menu.setMenuDescription(menuDescription);
	    menu.setCategory(category);
	    menu.setUpdatedAt(LocalDateTime.now());

	    menusRepository.save(menu);
	}
}
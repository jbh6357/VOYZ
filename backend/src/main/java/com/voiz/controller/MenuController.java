package com.voiz.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.voiz.service.MenuService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("api/menus")
@Tag(name = "menu", description = "메뉴관리 API")
public class MenuController {
	
	@Autowired
	private MenuService menuService;
	
	@PostMapping(value = "/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "메뉴판 OCR 인식", description = "메뉴판 사진을 입력받아 google vision API를 통해 메뉴 정보를 인식합니다.")
	public ResponseEntity<String> processOcrImage(
	        @RequestParam String userId,
	        @RequestParam String language,
	        @Parameter(description = "업로드할 이미지 파일", required = true)
    		@RequestPart("file") MultipartFile file) throws IOException {
		
		String result = menuService.sendToMlServer(file);
        return ResponseEntity.ok(result);
		
	}
	
	@PostMapping("/translate")
	@Operation(summary = "메뉴명 번역", description = "한글 메뉴명을 번역한 결과를 반환합니다. targetLanguage 코드 : 영어 en, 중국어(간체) zh-CN, 일본어 ja 등")
	public ResponseEntity<String> translateMenu(
			@RequestParam String menuName,
			@RequestParam String targetLanguage){
		
		String result = menuService.sendToMlServer(menuName, targetLanguage);
        return ResponseEntity.ok(result);
	}
	

}

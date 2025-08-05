package com.voiz.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.voiz.service.MenuService;
import com.voiz.vo.Menus;

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
	
	@PostMapping("/")
	@Operation(summary = "메뉴 등록", description = "메뉴를 등록합니다.")
	public ResponseEntity<String> createMenu(
			@RequestParam String userId,
			@RequestParam String menuName,
			@RequestParam int menuPrice){
		
		menuService.createMenu(userId, menuName, menuPrice);
		return ResponseEntity.ok().build(); 
	}
	
	@GetMapping("/{userId}")
	@Operation(summary = "사용자 메뉴 조회", description = "userId에 해당하는 모든 메뉴를 조회합니다.")
	public ResponseEntity<List<Menus>> getMenusByUserId(@PathVariable String userId) {
	    List<Menus> menus = menuService.getMenusByUserId(userId);
	    return ResponseEntity.ok(menus);
	}
	
	@PutMapping("/{menuIdx}")
	@Operation(summary = "메뉴 수정", description = "menuIdx에 해당하는 메뉴의 이름과 가격을 수정합니다.")
	public ResponseEntity<Void> updateMenu(
	        @PathVariable int menuIdx,
	        @RequestParam String menuName,
	        @RequestParam int menuPrice) {

	    menuService.updateMenu(menuIdx, menuName, menuPrice);
	    return ResponseEntity.ok().build();
	}
	
	@DeleteMapping("/{menuIdx}")
	@Operation(summary = "메뉴 삭제", description = "해당 메뉴를 삭제합니다.")
	public ResponseEntity<Void> deleteMenu(@PathVariable int menuIdx) {
	    menuService.deleteMenu(menuIdx);
	    return ResponseEntity.noContent().build(); 
	}
	
	@PostMapping(value = "/{menuIdx}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "메뉴 이미지 업로드", description = "메뉴 이미지를 업로드합니다.")
	public ResponseEntity<String> uploadMenuImage(
			@PathVariable int menuIdx,
			@Parameter(description = "업로드할 이미지 파일", required = true)
			@RequestPart("file") MultipartFile file) throws IOException {
		
		try {
	        String imageUrl = menuService.uploadMenuImage(menuIdx, file);
	        return ResponseEntity.ok("이미지 업로드 성공: " + imageUrl);
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	    } catch (IOException e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 저장 실패: " + e.getMessage());
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("알 수 없는 오류 발생");
	    }
	}

}

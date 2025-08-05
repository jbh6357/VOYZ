package com.voiz.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.google.zxing.WriterException;
import com.voiz.service.QrService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/qr")
@Tag(name = "qr", description = "QR 관리 API")
public class QrController {
	
	@Autowired
	private QrService qrService;
	
	@PostMapping("/generate")
	@Operation(summary = "QR 코드 URL 생성", description = "사용자 ID와 테이블 개수를 입력받아 테이블 DB에 QR 코드 URL을 저장합니다.")
	public ResponseEntity<String> generateQr(
	        @RequestParam String userId,
	        @RequestParam int number) {
		qrService.generateQRUrl(userId, number);
	    return ResponseEntity.ok().build();
	}
	
	@GetMapping("/{tableIdx}")
	@Operation(summary = "QR 코드 정보 조회", description = "테이블Idx를 입력받아 해당 테이블의 QR 코드를 반환합니다.")
	public ResponseEntity<byte[]> getQRCode(@PathVariable int tableIdx){
	  try {
	      byte[] qrCodeImage = qrService.generateQRCode(tableIdx);
	      
	      HttpHeaders headers = new HttpHeaders();
	      headers.setContentType(MediaType.IMAGE_PNG);
	      headers.set("Content-Disposition", "inline; filename=qrcode.png");
	      
	      return new ResponseEntity<>(qrCodeImage, headers, HttpStatus.OK);
	      
	  } catch (WriterException | IOException e) {
	      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	  }		
	}
	
}

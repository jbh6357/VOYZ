package com.voiz.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MenuService {

	@Autowired
	private FastApiClient fastApiClient;
	
	public String sendToMlServer(MultipartFile file) throws IOException {
		
		ResponseEntity<String> response =  fastApiClient.requestOcr(file);
		
		return response.getBody();
	}

	public String sendToMlServer(String menuName, String targetLanguage) {

		ResponseEntity<String> response = fastApiClient.requestTranslate(menuName, targetLanguage);
		
		return response.getBody();
	}
}

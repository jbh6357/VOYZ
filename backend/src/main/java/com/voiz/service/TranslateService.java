package com.voiz.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TranslateService {

	@Autowired
	private FastApiClient fastApiClient;
	
	public Map<String, Object> translateTexts(Map<String, Object> request) {
		return fastApiClient.translateTexts(request);
	}

}

package com.voiz.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.voiz.mapper.SpecialDayRepository;
import com.voiz.util.SpecialDayApi;
import com.voiz.vo.SpecialDay;

@Service
public class CollectorService {

	@Autowired
	private SpecialDayRepository specialDayRepository; 
	
	@Autowired
	private SpecialDayApi specialDayApi;
	
	public boolean collect(String year) {
		Map<String, String> endpoints = Map.of(
		        "공휴일", "getRestDeInfo",
		        "국경일", "getHoliDeInfo",
		        "절기", "get24DivisionsInfo",
		        "기념일", "getAnniversaryInfo",
		        "잡절", "getSundryDayInfo"
		    );
		
//		List<SpecialDay> allSpecialDays = new ArrayList<>();
		Map<String, SpecialDay> specialDayMap = new HashMap<>();
        try {
        	for (Map.Entry<String, String> entry : endpoints.entrySet()) {
        		for (int i = 1; i <= 12; i++) {
        		    String month = String.format("%02d", i); // "01", "02", ..., "12"
        		    String type = entry.getKey();       // "공휴일"
            	    String endpoint = entry.getValue();     // "getRestDeInfo"
            	    List<SpecialDay> result = specialDayApi.getSpecialDay(year, month, endpoint, type);
            	    
            	    for (SpecialDay day : result) {
            	    	 String key = day.getName();
            	    	 if (specialDayMap.containsKey(key)) {
            	                SpecialDay existing = specialDayMap.get(key);
            	                // 종료일 갱신: 더 큰 endDate 저장
            	                if (day.getEndDate().isAfter(existing.getEndDate())) {
            	                    existing.setEndDate(day.getEndDate());
            	                }
            	                
            	            } else {
            	                // 처음 들어오는 데이터는 복사해서 저장
            	                specialDayMap.put(key, day);
            	            }
            	    }
//            	    allSpecialDays.addAll(result);
        		} 		
        	}
        	// 최종 리스트로 변환
        	List<SpecialDay> allSpecialDays = new ArrayList<>(specialDayMap.values());
//			List<SpecialDay> result = specialDayApi.getSpecialDay(year, "06", "getRestDeInfo", "공휴일");
			specialDayRepository.saveAll(allSpecialDays);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}
	
}

package com.voiz.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.voiz.dto.FcmTokenDto;
import com.voiz.mapper.FcmRepository;
import com.voiz.vo.Fcm;


@Service
public class FcmTokenService {

	@Autowired
	private FcmRepository fcmRepository;
	
	public void save(FcmTokenDto dto) {
		 Optional<Fcm> existing = fcmRepository.findByUserId(dto.getUserId());
		 if (existing.isPresent()) {
		     System.out.println("토큰 이미 DB에 존재");   
		 } else {
			 Fcm fcm = new Fcm();
			 fcm.setUserId(dto.getUserId());
			 fcm.setToken(dto.getToken());
			 fcm.setUuid(dto.getUuid());
			 fcmRepository.save(fcm);
		 }
	}

	public void sendMessage(String token, String title, String body) {
		if (FirebaseApp.getApps().isEmpty()) {
	        throw new IllegalStateException("FirebaseApp이 초기화되지 않았습니다.");
	    }
		
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            throw new RuntimeException("FCM 메시지 전송 실패: " + e.getMessage(), e);
        }
    }
}

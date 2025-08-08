package com.voiz.config;

import java.io.InputStream;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.auth.oauth2.GoogleCredentials;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {
	@PostConstruct
    public void init(){
    	try{
        	InputStream serviceAccount = new ClassPathResource("voyz-6824b-firebase-adminsdk-fbsvc-9e11752bb7.json").getInputStream();
        	FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
         	if (FirebaseApp.getApps().isEmpty()) { // FirebaseApp이 이미 초기화되어 있지 않은 경우에만 초기화 실행
         		FirebaseApp app = FirebaseApp.initializeApp(options);
                FirebaseMessaging.getInstance(app); // 🔑 명시적으로 메시징 인스턴스를 초기화
                System.out.println("✅ FirebaseApp 초기화 완료");
            }
        } catch (Exception e){
        	e.printStackTrace();
        }
    }
}

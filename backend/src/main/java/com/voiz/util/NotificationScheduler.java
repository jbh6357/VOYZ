package com.voiz.util;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.voiz.mapper.FcmRepository;
import com.voiz.mapper.NotificationsRepository;
import com.voiz.service.FcmTokenService;
import com.voiz.vo.Fcm;
import com.voiz.vo.Notifications;

@Service
public class NotificationScheduler {

	@Autowired
	private NotificationsRepository notificationsRepository;
	
	@Autowired
	private FcmRepository fcmRepository;
	
	@Autowired
	private FcmTokenService fcmTokenService;
	
	@Scheduled(fixedRate = 60000) // 1분마다 실행
    public void checkAndSendAlerts() {
        List<Notifications> dueAlerts = notificationsRepository.findDueAlerts();
        System.out.println(dueAlerts);
        
        for (Notifications alert : dueAlerts) {
            try {
            	
            	Optional<Fcm> fcm = fcmRepository.findByUserId(alert.getUserId());
            	
                // FCM 전송
            	fcmTokenService.sendMessage(fcm.get().getToken(), alert.getTitle(), alert.getMessage());

                // 전송 상태 업데이트
                alert.setSent(1);
                notificationsRepository.save(alert);

                System.out.println("✅ 알림 전송 완료: " + alert.getUserId());

            } catch (Exception e) {
                e.printStackTrace();
                // 에러 로깅 또는 실패 상태 저장 가능
            }
        }
    }
}

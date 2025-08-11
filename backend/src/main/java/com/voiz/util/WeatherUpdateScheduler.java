package com.voiz.util;

import com.voiz.service.WeatherService;
import com.voiz.vo.Users;
import com.voiz.mapper.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class WeatherUpdateScheduler {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private UsersRepository usersRepository;

    /**
     * 매일 3시간 간격으로 모든 사용자의 위치에 대한 날씨 데이터를 업데이트합니다.
     * (예: 새벽 3시 10분, 아침 6시 10분, 9시 10분...)
     * 기상청 단기예보 발표 시각(02시, 05시, 08시...) 직후에 실행되도록 설정합니다.
     */
    @Scheduled(cron = "0 10 2,5,8,11,14,17,20,23 * * *")
    public void updateAllUsersWeatherData() {
        System.out.println("✅ [스케줄러 시작] 모든 사용자의 날씨 데이터 업데이트를 시작합니다.");

        // 1. DB에서 모든 사용자 정보를 가져옵니다.
        List<Users> allUsers = usersRepository.findAll();

        // 2. 주소만 추출하고, 중복된 주소를 제거합니다.
        List<String> uniqueAddresses = allUsers.stream()
                                               .map(Users::getStoreAddress)
                                               .distinct()
                                               .collect(Collectors.toList());

        System.out.println("-> 총 " + uniqueAddresses.size() + "개의 고유한 주소에 대한 날씨 정보 업데이트를 시도합니다.");

        // 3. 각각의 고유한 주소에 대해 날씨 데이터 수집 API를 호출합니다.
        for (String address : uniqueAddresses) {
            try {
                System.out.println("-> 주소: '" + address + "' 날씨 데이터 수집 중...");
                weatherService.weatherData(address);
                System.out.println("   ... 수집 성공!");
            } catch (Exception e) {
                // 특정 주소에서 에러가 발생하더라도 다른 주소는 계속 처리되도록 합니다.
                System.err.println("❗️ 주소: '" + address + "' 날씨 데이터 수집 실패: " + e.getMessage());
            }
        }

        System.out.println("✅ [스케줄러 종료] 날씨 데이터 업데이트가 완료되었습니다.");
    }
}
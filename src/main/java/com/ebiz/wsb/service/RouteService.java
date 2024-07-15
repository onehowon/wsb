package com.ebiz.wsb.service;

// 경로 이탈 경고 서비스

import com.ebiz.wsb.model.Location;
import org.springframework.stereotype.Service;

@Service
public class RouteService {
    // 경로 이탈 감지하는 로직
    public boolean isOffRoute(Location location, double allowedDeviation){
        // 경로와 현재 위치 간 거리 계산 로직을 구현
        // allowedDeviation 안에 있는지 확인
        // true / false만 반환해보는 test code
        return false;
    }

    // 경고 알림 전송 로직
    public void sendWarningAlert(Location location){
        // 경고 알림 전송 로직 구현
        // ex) 이메일, sms, 앱 푸시 알림 등 추가 해야함
    }
}

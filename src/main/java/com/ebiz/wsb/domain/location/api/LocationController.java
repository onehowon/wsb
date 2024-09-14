package com.ebiz.wsb.domain.location.api;

import com.ebiz.wsb.domain.location.dto.LocationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class LocationController {
    @MessageMapping("/location")
    @SendTo("/topic/locations")
    public LocationDTO sendLocation(LocationDTO location) {
        log.info("현재 위치: Latitude={}, Longitude={}", location.getLatitude(), location.getLongitude());
        // 받은 위치 데이터를 다른 사용자에게 브로드캐스팅
        return location;
    }
}

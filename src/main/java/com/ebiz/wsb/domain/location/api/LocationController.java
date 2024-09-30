package com.ebiz.wsb.domain.location.api;

import com.ebiz.wsb.domain.location.dto.LocationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LocationController {

    private final SimpMessagingTemplate messagingTemplate;
    @MessageMapping("/location/{groupId}")
    public void sendLocation(@DestinationVariable String groupId, LocationDTO location) {
        log.info("그룹 {}의 현재 위치: Latitude={}, Longitude={}", groupId, location.getLatitude(), location.getLongitude());

        messagingTemplate.convertAndSend("/topic/locations/" + groupId, location);
    }
}

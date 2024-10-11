package com.ebiz.wsb.domain.notification.api;

import com.ebiz.wsb.domain.notification.application.NotificationService;
import com.ebiz.wsb.domain.notification.dto.GuardianNotificationDTO;
import com.ebiz.wsb.domain.notification.dto.ParentNotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/guardian")
    public ResponseEntity<GuardianNotificationDTO> createGuardianNotification(
            @RequestBody Map<String, String> requestBody,
            Authentication authentication) {

        String type = requestBody.get("type");
        String content = requestBody.get("content");

        GuardianNotificationDTO createdNotification = notificationService.createGuardianNotification(type, content, authentication);
        return ResponseEntity.ok(createdNotification);
    }

    @PostMapping("/parent")
    public ResponseEntity<ParentNotificationDTO> createParentNotification(
            @RequestBody Map<String, String> requestBody,
            Authentication authentication) {

        String type = requestBody.get("type");
        String content = requestBody.get("content");

        ParentNotificationDTO createdNotification = notificationService.createParentNotification(type, content, authentication);
        return ResponseEntity.ok(createdNotification);
    }
}
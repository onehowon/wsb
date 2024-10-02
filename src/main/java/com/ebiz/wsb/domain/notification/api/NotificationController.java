package com.ebiz.wsb.domain.notification.api;

import com.ebiz.wsb.domain.notification.application.NotificationService;
import com.ebiz.wsb.domain.notification.dto.GuardianNotificationDTO;
import com.ebiz.wsb.domain.notification.dto.ParentNotificationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/guardian")
    public ResponseEntity<GuardianNotificationDTO> createGuardianNotification(@RequestBody GuardianNotificationDTO dto) {
        GuardianNotificationDTO createdNotification = notificationService.createGuardianNotification(dto);
        return ResponseEntity.ok(createdNotification);
    }

    @PostMapping("/parent")
    public ResponseEntity<ParentNotificationDTO> createParentNotification(@RequestBody ParentNotificationDTO dto) {
        ParentNotificationDTO createdNotification = notificationService.createParentNotification(dto);
        return ResponseEntity.ok(createdNotification);
    }
}
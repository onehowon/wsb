package com.ebiz.wsb.domain.notification.api;

import com.ebiz.wsb.domain.notification.application.NotificationService;
import com.ebiz.wsb.domain.notification.application.PushNotificationService;
import com.ebiz.wsb.domain.notification.entity.FcmToken;
import com.ebiz.wsb.domain.notification.entity.Notification;
import com.ebiz.wsb.domain.notification.entity.NotificationType;
import com.ebiz.wsb.domain.notification.entity.UserType;
import com.ebiz.wsb.domain.notification.repository.FcmTokenRepository;
import com.ebiz.wsb.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(
            @RequestParam NotificationType type,
            @RequestParam String body) {

        notificationService.sendNotification(type, body);

        return ResponseEntity.ok("알림 전송 및 로그 저장 완료");
    }
}
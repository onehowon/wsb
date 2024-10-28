package com.ebiz.wsb.domain.notification.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.notification.entity.FcmToken;
import com.ebiz.wsb.domain.notification.entity.Notification;
import com.ebiz.wsb.domain.notification.entity.NotificationType;
import com.ebiz.wsb.domain.notification.entity.UserType;
import com.ebiz.wsb.domain.notification.repository.FcmTokenRepository;
import com.ebiz.wsb.domain.notification.repository.NotificationRepository;
import com.ebiz.wsb.domain.parent.entity.Parent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final PushNotificationService pushNotificationService;
    private final FcmTokenRepository fcmTokenRepository;
    private final NotificationRepository notificationRepository;
    private final UserDetailsServiceImpl userDetailsService;

    public void sendNotification(NotificationType type, String body) {
        Object currentUser = userDetailsService.getUserByContextHolder();
        Long userId;
        UserType userType;

        if (currentUser instanceof Guardian guardian) {
            userId = guardian.getId();
            userType = UserType.GUARDIAN;
        } else if (currentUser instanceof Parent parent) {
            userId = parent.getId();
            userType = UserType.PARENT;
        } else {
            throw new IllegalArgumentException("인증된 사용자 정보가 유효하지 않습니다.");
        }

        FcmToken fcmToken = fcmTokenRepository.findByUserIdAndUserType(userId, userType)
                .orElseThrow(() -> new RuntimeException("FCM 토큰을 찾을 수 없습니다."));

        try {
            pushNotificationService.sendPushNotification(type.name(), body, Map.of("type", type.name()), fcmToken.getToken());
        } catch (IOException | InterruptedException e) {
            log.error("알림 전송 실패", e);
            throw new RuntimeException("알림 전송 실패: " + e.getMessage());
        }

        Notification notification = Notification.builder()
                .userId(userId)
                .userType(userType)
                .type(type)
                .body(body)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }
}

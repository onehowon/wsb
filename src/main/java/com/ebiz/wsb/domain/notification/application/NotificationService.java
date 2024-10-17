package com.ebiz.wsb.domain.notification.application;

import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.exception.GuardianNotFoundException;
import com.ebiz.wsb.domain.guardian.repository.GuardianRepository;
import com.ebiz.wsb.domain.notification.dto.GuardianNotificationDTO;
import com.ebiz.wsb.domain.notification.dto.ParentNotificationDTO;
import com.ebiz.wsb.domain.notification.entity.GuardianNotification;
import com.ebiz.wsb.domain.notification.entity.NotificationType;
import com.ebiz.wsb.domain.notification.entity.ParentNotification;
import com.ebiz.wsb.domain.notification.exception.NotificationTypeNotFoundException;
import com.ebiz.wsb.domain.notification.repository.GuardianNotificationRepository;
import com.ebiz.wsb.domain.notification.repository.NotificationTypeRepository;
import com.ebiz.wsb.domain.notification.repository.ParentNotificationRepository;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.parent.exception.ParentNotFoundException;
import com.ebiz.wsb.domain.parent.repository.ParentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final GuardianNotificationRepository guardianNotificationRepository;
    private final ParentNotificationRepository parentNotificationRepository;
    private final NotificationTypeRepository notificationTypeRepository;
    private final GuardianRepository guardianRepository;
    private final ParentRepository parentRepository;

    public GuardianNotificationDTO createGuardianNotification(String type, String content, Authentication authentication) {
        Guardian guardian = getAuthenticatedGuardian(authentication);

        NotificationType notificationType = notificationTypeRepository.findByNameAndTarget(type, "GUARDIAN")
                .orElseThrow(() -> new NotificationTypeNotFoundException("유효하지 않은 알림 타입: " + type));

        GuardianNotification notification = GuardianNotification.builder()
                .guardian(guardian)
                .notificationType(notificationType)
                .content(content)
                .build();

        guardianNotificationRepository.save(notification);

        return GuardianNotificationDTO.builder()
                .content(notification.getContent())
                .type(notification.getNotificationType().getName())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public ParentNotificationDTO createParentNotification(String type, String content, Authentication authentication) {
        Parent parent = getAuthenticatedParent(authentication);

        NotificationType notificationType = notificationTypeRepository.findByNameAndTarget(type, "PARENT")
                .orElseThrow(() -> new NotificationTypeNotFoundException("유효하지 않은 알림 타입: " + type));

        ParentNotification notification = ParentNotification.builder()
                .parent(parent)
                .notificationType(notificationType)
                .content(content)
                .build();

        parentNotificationRepository.save(notification);

        return ParentNotificationDTO.builder()
                .content(notification.getContent())
                .type(notification.getNotificationType().getName())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private Guardian getAuthenticatedGuardian(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return guardianRepository.findGuardianByEmail(userDetails.getUsername())
                .orElseThrow(() -> new GuardianNotFoundException("인증된 지도사 정보를 찾을 수 없습니다."));
    }

    private Parent getAuthenticatedParent(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return parentRepository.findParentByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ParentNotFoundException("인증된 부모 정보를 찾을 수 없습니다."));
    }

    // 아래부터 나중에 쓸 수도 있어서 삭제 XX
    private GuardianNotificationDTO convertToGuardianNotificationDTO(GuardianNotification notification) {
        return GuardianNotificationDTO.builder()
                .content(notification.getContent())
                .type(notification.getNotificationType().getName())
                .createdAt(notification.getCreatedAt())
                .build();
    }


    private ParentNotificationDTO convertToParentNotificationDTO(ParentNotification notification) {
        return ParentNotificationDTO.builder()
                .content(notification.getContent())
                .type(notification.getNotificationType().getName())
                .createdAt(notification.getCreatedAt())
                .build();
    }

}

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private GuardianNotificationRepository guardianNotificationRepository;

    @Autowired
    private ParentNotificationRepository parentNotificationRepository;

    @Autowired
    private NotificationTypeRepository notificationTypeRepository;

    @Autowired
    private GuardianRepository guardianRepository;

    @Autowired
    private ParentRepository parentRepository;

    public GuardianNotificationDTO createGuardianNotification(GuardianNotificationDTO dto) {
        NotificationType type = notificationTypeRepository.findByName(dto.getType())
                .orElseThrow(() -> new NotificationTypeNotFoundException("알림 타입을 찾을 수 없습니다: " + dto.getType()));

        Guardian guardian = guardianRepository.findGuardianByEmail(dto.getGuardianEmail())
                .orElseThrow(() -> new GuardianNotFoundException("인솔자 정보를 찾을 수 없습니다."));

        GuardianNotification notification = GuardianNotification.builder()
                .guardian(guardian)
                .notificationType(type)
                .content(dto.getContent())
                .build();

        guardianNotificationRepository.save(notification);

        // DTO로 반환
        return GuardianNotificationDTO.builder()
                .notificationId(notification.getNotificationId())
                .guardianId(guardian.getId())
                .guardianEmail(guardian.getEmail())
                .content(notification.getContent())
                .type(notification.getNotificationType().getName())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public ParentNotificationDTO createParentNotification(ParentNotificationDTO dto) {
        NotificationType type = notificationTypeRepository.findByName(dto.getType())
                .orElseThrow(() -> new NotificationTypeNotFoundException("알림 타입을 찾을 수 없습니다: " + dto.getType()));

        Parent parent = parentRepository.findParentByEmail(dto.getParentEmail())
                .orElseThrow(() -> new ParentNotFoundException("부모 정보를 찾을 수 없습니다."));

        ParentNotification notification = ParentNotification.builder()
                .parent(parent)
                .notificationType(type)
                .content(dto.getContent())
                .build();

        parentNotificationRepository.save(notification);

        return ParentNotificationDTO.builder()
                .notificationId(notification.getNotificationId())
                .parentId(parent.getId())
                .parentEmail(parent.getEmail())
                .content(notification.getContent())
                .type(notification.getNotificationType().getName())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}

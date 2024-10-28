package com.ebiz.wsb.domain.notification.repository;

import com.ebiz.wsb.domain.notification.entity.Notification;
import com.ebiz.wsb.domain.notification.entity.NotificationType;
import com.ebiz.wsb.domain.notification.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdAndUserType(Long userId, UserType userType);
    List<Notification> findByUserIdAndUserTypeAndType(Long userId, UserType userType, NotificationType type);
}

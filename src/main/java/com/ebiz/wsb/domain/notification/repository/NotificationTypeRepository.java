package com.ebiz.wsb.domain.notification.repository;

import com.ebiz.wsb.domain.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTypeRepository extends JpaRepository<NotificationType, Long> {
    // "PRESENT" 나 "ABSENT" 등을 찾기 위한 메서드
    Optional<NotificationType> findByName(String name);
}

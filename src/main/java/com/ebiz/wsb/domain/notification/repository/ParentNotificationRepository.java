package com.ebiz.wsb.domain.notification.repository;

import com.ebiz.wsb.domain.notification.entity.ParentNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentNotificationRepository extends JpaRepository<ParentNotification, Long> {
}

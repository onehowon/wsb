package com.ebiz.wsb.domain.notification.repository;

import com.ebiz.wsb.domain.notification.entity.GuardianNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuardianNotificationRepository extends JpaRepository<GuardianNotification, Long> {
}

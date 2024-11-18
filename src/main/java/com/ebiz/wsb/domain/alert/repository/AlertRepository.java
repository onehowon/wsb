package com.ebiz.wsb.domain.alert.repository;

import com.ebiz.wsb.domain.alert.entity.Alert;
import com.ebiz.wsb.domain.notification.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByReceiverIdAndUserType(Long receiverId, UserType userType);
}

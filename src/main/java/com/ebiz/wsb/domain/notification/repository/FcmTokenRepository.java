package com.ebiz.wsb.domain.notification.repository;

import com.ebiz.wsb.domain.notification.entity.FcmToken;
import com.ebiz.wsb.domain.notification.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByUserIdAndUserType(Long userId, UserType userType);
    void deleteByUserIdAndUserType(Long userId, UserType userType);
}

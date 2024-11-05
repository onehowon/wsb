package com.ebiz.wsb.domain.notification.repository;

import com.ebiz.wsb.domain.notification.entity.FcmToken;
import com.ebiz.wsb.domain.notification.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByUserId(Long UserId);

    List<FcmToken> findAllByUserId(Long userId);
    void deleteByUserId(Long userId);

    Optional<FcmToken> findByToken(String token);
    List<FcmToken> findByUserIdAndUserType(Long userId, UserType userType);
}

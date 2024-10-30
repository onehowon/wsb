package com.ebiz.wsb.domain.alert.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.alert.entity.Alert;
import com.ebiz.wsb.domain.alert.exception.AlertNotFoundException;
import com.ebiz.wsb.domain.alert.repository.AlertRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AlertService {
    private final UserDetailsServiceImpl userDetailsService;
    private final AlertRepository alertRepository;

    @Transactional
    public Alert createAlert(Long userId, Alert.AlertCategory category, String title, String content) {
        Object user = userDetailsService.getUserByContextHolder();

        Long receiverId;
        if (user instanceof Guardian) {
            receiverId = ((Guardian) user).getId();
        } else if (user instanceof Parent) {
            receiverId = ((Parent) user).getId();
        } else {
            throw new IllegalArgumentException("알 수 없는 사용자 타입입니다.");
        }

        Alert alert = Alert.builder()
                .receiverId(receiverId)
                .alertCategory(category)
                .title(title)
                .content(content)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();

        return alertRepository.save(alert);
    }

    @Transactional
    public void markAsRead(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new AlertNotFoundException("알림을 찾을 수 없습니다."));
        alert.read();
        alertRepository.save(alert);
    }
}

package com.ebiz.wsb.domain.alert.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.notification.entity.UserType;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.alert.entity.Alert;
import com.ebiz.wsb.domain.alert.exception.AlertNotFoundException;
import com.ebiz.wsb.domain.alert.repository.AlertRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {
    private final UserDetailsServiceImpl userDetailsService;
    private final AlertRepository alertRepository;

    @Transactional
    public void createAlert(Long receiverId, Alert.AlertCategory category, String alarmTitle, String alarmContent, UserType userType) {
        Alert alert = Alert.builder()
                .receiverId(receiverId)
                .alertCategory(category)
                .title(alarmTitle)
                .content(alarmContent)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .userType(userType)
                .build();

        alertRepository.save(alert);
    }

    @Transactional
    public List<Alert> getAlertsForCurrentUser(){
        Object currentUser = userDetailsService.getUserByContextHolder();
        Long receiverId;

        if (currentUser instanceof Parent) {
            receiverId = ((Parent) currentUser).getId();
        } else if (currentUser instanceof Guardian) {
            receiverId = ((Guardian) currentUser).getId();
        } else {
            throw new IllegalArgumentException("알 수 없는 사용자 타입입니다.");
        }

        return alertRepository.findByReceiverId(receiverId);
    }

    @Transactional
    public void markAsRead(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new AlertNotFoundException("알림을 찾을 수 없습니다."));
        alert.read();
        alertRepository.save(alert);
    }
}

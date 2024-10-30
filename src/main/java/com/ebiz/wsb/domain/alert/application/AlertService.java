package com.ebiz.wsb.domain.alert.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.alert.entity.Alert;
import com.ebiz.wsb.domain.alert.exception.AlertNotFoundException;
import com.ebiz.wsb.domain.alert.repository.AlertRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {
    private final UserDetailsServiceImpl userDetailsService;
    private final AlertRepository alertRepository;

    @Transactional
    public Alert createAlert(Long receiverId, Alert.AlertCategory category, String title, String content) {
        log.info("createAlert 호출: receiverId={}, category={}, title={}, content={}", receiverId, category, title, content);

        Alert alert = Alert.builder()
                .receiverId(receiverId)
                .alertCategory(category)
                .title(title)
                .content(content)
                .createdAt(LocalDateTime.now())
                .isRead(false)
                .build();

        Alert savedAlert = alertRepository.save(alert);
        log.info("Alert 저장 완료: alertId={}", savedAlert.getId()); // 저장 완료 확인
        return savedAlert;
    }





    @Transactional
    public void markAsRead(Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new AlertNotFoundException("알림을 찾을 수 없습니다."));
        alert.read();
        alertRepository.save(alert);
    }
}

package com.ebiz.wsb.domain.alert.dto;

import com.ebiz.wsb.domain.alert.entity.Alert;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AlertDTO {

    private Long id;
    private String title;
    private String content;
    private String category;
    private LocalDateTime createdAt;
    private boolean isRead;

    public static AlertDTO from(Alert alert) {
        return AlertDTO.builder()
                .id(alert.getId())
                .title(alert.getTitle())
                .content(alert.getContent())
                .category(alert.getAlertCategory().toString())
                .createdAt(alert.getCreatedAt())
                .isRead(alert.isRead())
                .build();
    }
}

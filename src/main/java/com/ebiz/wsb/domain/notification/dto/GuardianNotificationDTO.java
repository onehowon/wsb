package com.ebiz.wsb.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GuardianNotificationDTO {
    private Long notificationId;
    private Long guardianId;
    private String guardianEmail;
    private String content;
    private String type;
    private LocalDateTime createdAt;
}
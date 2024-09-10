package com.ebiz.wsb.domain.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
@Table(name = "GuardianNotification")
public class GuardianNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @Column(name = "guardian_id")
    private Long guardianId;

    @Column(name = "content")
    private String content;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private NotificationType notificationType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
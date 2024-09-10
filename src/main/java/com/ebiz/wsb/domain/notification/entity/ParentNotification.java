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
@Table(name = "ParentNotification")
public class ParentNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "content")
    private String content;

    @ManyToOne
    @JoinColumn(name = "type_id", nullable = false)
    private NotificationType notificationType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

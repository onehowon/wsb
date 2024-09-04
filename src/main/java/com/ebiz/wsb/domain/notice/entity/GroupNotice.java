package com.ebiz.wsb.domain.notice.entity;


import com.ebiz.wsb.domain.guardian.entity.Guardian;
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
@Table(name = "GroupNotice")
public class GroupNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_notice_id")
    private Long groupNoticeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_type_id", nullable = false)
    private NoticeType noticeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id", nullable = false)
    private Guardian guardian;

    @Column(name = "content")
    private String content;

    @Column(name = "photo")
    private String photo;

    @Column(name = "likes")
    private int likes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;


}

package com.ebiz.wsb.domain.notice.dto;


import com.ebiz.wsb.domain.guardian.dto.GuardianDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupNoticeDTO {
    private NoticeTypeDTO noticeType;
    private String content;
    private String photo;
    private Integer likes;
    private LocalDateTime createdAt;
}

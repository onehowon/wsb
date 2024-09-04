package com.ebiz.wsb.domain.notice.dto;


import com.ebiz.wsb.domain.guardian.dto.GuardianDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupNoticeDTO {

    private Long groupNoticeId;
    private NoticeTypeDTO noticeType;
    private GuardianDTO guardian;
    private String content;
    private String photo;
    private int likes;
    private String createdAt;


}

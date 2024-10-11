package com.ebiz.wsb.domain.message.dto;

import com.ebiz.wsb.domain.group.dto.GroupDTO;
import com.ebiz.wsb.domain.parent.dto.ParentDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {

    private Long messageId;
    private GroupDTO group;
    private ParentDTO parent;
    private String content;
    private LocalDateTime transferredAt;

}

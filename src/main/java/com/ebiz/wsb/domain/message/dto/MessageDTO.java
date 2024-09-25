package com.ebiz.wsb.domain.message.dto;

import com.ebiz.wsb.domain.guardian.dto.GuardianDTO;
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
    private GuardianDTO guardian;
    private ParentDTO parent;
    private String content;
    private LocalDateTime transferredAt;

}

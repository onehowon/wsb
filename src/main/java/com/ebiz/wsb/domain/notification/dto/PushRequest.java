package com.ebiz.wsb.domain.notification.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PushRequest {
    private String title;
    private String body;
}

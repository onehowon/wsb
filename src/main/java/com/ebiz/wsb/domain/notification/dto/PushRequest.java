package com.ebiz.wsb.domain.notification.dto;

import com.ebiz.wsb.domain.alert.entity.Alert;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PushRequest {
    private String title;
    private String body;
    private Alert.AlertCategory alertCategory;
}

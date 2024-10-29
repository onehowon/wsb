package com.ebiz.wsb.domain.sse.application;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AlertService {
    SseEmitter subscribe();

    void sendToClient(Long targetId, String category, Object data);

    void read(Long id);
}

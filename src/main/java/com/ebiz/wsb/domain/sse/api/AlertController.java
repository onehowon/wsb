package com.ebiz.wsb.domain.sse.api;

import com.ebiz.wsb.domain.sse.application.AlertServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/alert")
public class AlertController {
    private final AlertServiceImpl alertService;

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect() {
        SseEmitter emitter = alertService.subscribe();
        return ResponseEntity.ok(emitter);
    }

    @GetMapping("/disconnect")
    public void disconnect() {
        alertService.unsubscribe();
    }

    @GetMapping("/{id}")
    public void getAlertDetail(@PathVariable Long id) {

    }
}

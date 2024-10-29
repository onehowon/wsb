package com.ebiz.wsb.domain.sse.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.sse.entity.Alert;
import com.ebiz.wsb.domain.sse.exception.AlertNotFoundException;
import com.ebiz.wsb.domain.sse.repository.AlertRepository;
import com.ebiz.wsb.domain.sse.repository.SseRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService{

    private final UserDetailsServiceImpl userDetailsService;
    private final SseRepository sseRepository;
    private final AlertRepository alertRepository;

    @Override
    @Transactional
    public SseEmitter subscribe(){
        Object user = userDetailsService.getUserByContextHolder();

        Long userId;
        if (user instanceof Guardian) {
            userId = ((Guardian) user).getId();
        } else if (user instanceof Parent) {
            userId = ((Parent) user).getId();
        } else {
            throw new IllegalArgumentException("알 수 없는 사용자 타입입니다.");
        }

        SseEmitter existingEmitter = sseRepository.findById(userId);

        if(existingEmitter != null){
            existingEmitter.complete();
            sseRepository.deleteById(userId);
        }

        SseEmitter emitter = new SseEmitter(1000 * 60L);
        sseRepository.save(userId, emitter);
        try{
            emitter.send(SseEmitter.event()
                    .name("CONNECT")
                    .data("SSE 연결 성공"));
        } catch (IOException e){
            throw new RuntimeException(e);
        }

        emitter.onCompletion(() -> {
            emitter.complete();
            sseRepository.deleteById(userId);
        });

        return emitter;
    }

    public void unsubscribe(){
        Object user = userDetailsService.getUserByContextHolder();

        Long userId;
        if (user instanceof Guardian) {
            userId = ((Guardian) user).getId();
        } else if (user instanceof Parent) {
            userId = ((Parent) user).getId();
        } else {
            throw new IllegalArgumentException("알 수 없는 사용자 타입입니다.");
        }
        SseEmitter emitter = sseRepository.findById(userId);
        emitter.complete();
        if(userId != null){
            sseRepository.deleteById(userId);
        }
    }

    @Override
    public void sendToClient(Long targetId, String category, Object data){
        SseEmitter emitter = sseRepository.findById(targetId);
        if(emitter != null){
            try{
                emitter.send(SseEmitter.event()
                        .name(category)
                        .data(data));
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    @Transactional
    @Override
    public void read(Long id){
        Alert alert = alertRepository.findById(id).orElseThrow(() -> new AlertNotFoundException("찾을 수 없는 알림입니다."));
        alert.read();
    }
}

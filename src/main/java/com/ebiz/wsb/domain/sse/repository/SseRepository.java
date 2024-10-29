package com.ebiz.wsb.domain.sse.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseRepository {
    private Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void save(Long id, SseEmitter emitter) {
        emitters.put(id,emitter);
    }

    public void deleteById(Long id){
        emitters.remove(id);
    }

    public SseEmitter findById(Long id){
        return emitters.get(id);
    }

    public int size(){
        return emitters.size();
    }
}

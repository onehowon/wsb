package com.ebiz.wsb.attendance;

import com.ebiz.wsb.domain.attendance.dto.AttendanceDTO;
import com.ebiz.wsb.domain.attendance.dto.AttendanceStatusDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AttendanceWebSocketTest {

    @LocalServerPort
    private int port;
    @Test
    public void testWebSocketMessaging() throws Exception {
        // ObjectMapper에 JavaTimeModule 추가
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // MappingJackson2MessageConverter에 ObjectMapper 적용
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper); // 커스텀 ObjectMapper 설정

        // WebSocketStompClient에 메시지 컨버터 설정
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(converter); // 잘못된 부분 수정

        // WebSocket 연결 및 구독
        StompSession session = stompClient.connect("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        session.subscribe("/sub/attendance/updates", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return AttendanceDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                AttendanceDTO message = (AttendanceDTO) payload;
                assertEquals("출석", message.getStatus());  // 메시지 검증
            }
        });

        // 메시지 전송
        session.send("/pub/attendance/update", new AttendanceStatusDTO("출석", LocalDateTime.now(), LocalDateTime.now()));
    }
}

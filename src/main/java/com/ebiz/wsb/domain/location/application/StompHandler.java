package com.ebiz.wsb.domain.location.application;

import com.ebiz.wsb.domain.auth.application.JwtProvider;
import com.ebiz.wsb.domain.location.exception.InvalidLocationDataException;
import com.ebiz.wsb.domain.token.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if(StompCommand.CONNECT.equals(accessor.getCommand())) {
            String accessToken = accessor.getFirstNativeHeader("accessToken");
            if(accessToken == null || !jwtProvider.validateToken(accessToken)) {
                throw new InvalidTokenException("유효하지 않은 토큰");
            }

        }

//        if (StompCommand.SEND.equals(accessor.getCommand())) {
//            String latitude = accessor.getFirstNativeHeader("latitude");
//            String longitude = accessor.getFirstNativeHeader("longitude");
//
//            if (latitude == null || longitude == null) {
//                throw new InvalidLocationDataException("위도와 경도 값이 필요합니다.");
//            }
//
//            // 위치 데이터 검증 (예: 값 범위 검증)
//            log.info("수신 받은 위경도값: Latitude={}, Longitude={}", latitude, longitude);
//
//            // 토큰 검증
//            String accessToken = accessor.getFirstNativeHeader("accessToken");
//            if (accessToken == null || !jwtProvider.validateToken(accessToken)) {
//                throw new InvalidTokenException("유효하지 않은 토큰");
//            }
//        }

        return message;
    }


}

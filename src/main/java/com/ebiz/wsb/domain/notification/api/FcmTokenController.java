package com.ebiz.wsb.domain.notification.api;

import com.amazonaws.Response;
import com.ebiz.wsb.domain.notification.entity.FcmToken;
import com.ebiz.wsb.domain.notification.entity.UserType;
import com.ebiz.wsb.domain.notification.repository.FcmTokenRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fcm")
@RequiredArgsConstructor
@Builder
public class FcmTokenController {

    private final FcmTokenRepository fcmTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<String> registerToken(@RequestParam Long userId, @RequestParam UserType userType, @RequestParam String token) {
        fcmTokenRepository.findByUserIdAndUserType(userId, userType)
                .ifPresent(fcmTokenRepository::delete);

        FcmToken fcmToken = FcmToken.builder()
                .userId(userId)
                .userType(userType)
                .token(token)
                .build();
        fcmTokenRepository.save(fcmToken);
        return ResponseEntity.ok("FCM 토큰 등록 완료");
    }

    @DeleteMapping("/remove")
    public ResponseEntity<String> removeToken(@RequestParam Long userId, @RequestParam UserType userType) {
        fcmTokenRepository.deleteByUserIdAndUserType(userId, userType);
        return ResponseEntity.ok("FCM 토큰 삭제 완료");
    }
}

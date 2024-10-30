package com.ebiz.wsb.domain.notification.application;

import com.ebiz.wsb.domain.alert.application.AlertService;
import com.ebiz.wsb.domain.alert.entity.Alert;
import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.notification.dto.AndroidNotificationDTO;
import com.ebiz.wsb.domain.notification.dto.FcmMessage;
import com.ebiz.wsb.domain.notification.dto.FcmMessage.Notification;
import com.ebiz.wsb.domain.notification.dto.NotificationDTO;
import com.ebiz.wsb.domain.notification.entity.FcmToken;
import com.ebiz.wsb.domain.notification.repository.FcmTokenRepository;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.net.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import okhttp3.RequestBody;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushNotificationService {

    private final UserDetailsServiceImpl userDetailsService;
    private final FcmTokenRepository fcmTokenRepository;
    private final ObjectMapper objectMapper;
    private final AlertService alertService;
    private final OkHttpClient client = new OkHttpClient();

    @Value("${fcm.project.id}")
    private String PROJECT_ID;
    private static final String FCM_SEND_URL = "https://fcm.googleapis.com/v1/projects/%s/messages:send";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    public void sendPushMessage(String title, String body, Map<String, String> data, String token, Alert.AlertCategory category)
            throws IOException {
        String message = makeMessage(title, body, data, token);
        RequestBody requestBody = RequestBody.create(JSON_MEDIA_TYPE, message);
        Request request = new Request.Builder()
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                .post(requestBody)
                .url(String.format(FCM_SEND_URL, PROJECT_ID))
                .build();
        Response response = client.newCall(request).execute();
        response.close();

        Object user = userDetailsService.getUserByContextHolder();
        Long userId;

        if (user instanceof Guardian) {
            userId = ((Guardian) user).getId();
        } else if (user instanceof Parent) {
            userId = ((Parent) user).getId();
        } else {
            throw new IllegalArgumentException("알 수 없는 사용자 타입입니다.");
        }
        alertService.createAlert(userId, category, title, body);
    }

    private String makeMessage(String title, String body, Map<String, String> data, String token)
            throws JsonProcessingException {
        AndroidNotificationDTO android = new AndroidNotificationDTO();
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setTitle(title);
        notificationDTO.setBody(body);
        android.setNotification(notificationDTO);

        FcmMessage fcmMessage = FcmMessage.builder()
                .message(FcmMessage.Message.builder()
                        .token(token)
                        .data(data)
                        .android(android)
                        .notification(Notification.builder()
                                .title(title)
                                .body(body)
                                .build())
                        .build())
                .build();

        return objectMapper.writeValueAsString(fcmMessage);
    }

    private String getAccessToken() throws IOException {
        ClassPathResource classPathResource = new ClassPathResource(
                "firebase/firebase-adminsdk.json");

        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(classPathResource.getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    public void save(String token) {
        Object user = userDetailsService.getUserByContextHolder();

        Long userId;
        if (user instanceof Guardian) {
            userId = ((Guardian) user).getId();
        } else if (user instanceof Parent) {
            userId = ((Parent) user).getId();
        } else {
            throw new IllegalArgumentException("알 수 없는 사용자 타입입니다.");
        }

        FcmToken fcmToken = FcmToken.builder()
                .userId(userId)
                .token(token)
                .build();
        fcmTokenRepository.save(fcmToken);
    }
}

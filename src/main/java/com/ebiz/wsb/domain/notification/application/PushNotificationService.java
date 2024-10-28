package com.ebiz.wsb.domain.notification.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PushNotificationService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    private static final String FCM_SEND_URL = "https://fcm.googleapis.com/v1/projects/donghang-c8fca/messages:send";

    public void sendPushNotification(String title, String body, Map<String, String> data, String token) throws IOException, InterruptedException{
        try {
            String message = createMessage(title, body, data, token);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(FCM_SEND_URL))
                    .header("Authorization", "Bearer " + getAccessToken())
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(message))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("FCM 푸시 알림 전송 실패. 상태 코드: {}, 응답 내용: {}", response.statusCode(), response.body());
                throw new IOException("FCM 푸시 알림 전송 실패: " + response.body());
            }
            log.info("FCM 푸시 알림 전송 성공: {}", response.body());
        } catch (IOException | InterruptedException e) {
            log.error("FCM 푸시 알림 전송 중 예외 발생", e);
            Thread.currentThread().interrupt();
        }
    }

    private String createMessage(String title, String body, Map<String, String> data, String token) throws IOException{
        var notification = Map.of(
                "title", title,
                "body", body
        );

        var message = Map.of(
                "message", Map.of(
                        "token", token,
                        "notification", notification,
                        "data", data
                )
        );

        return objectMapper.writeValueAsString(message);
    }

    private String getAccessToken() throws IOException{
        try(InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("firebase/firebase-adminsdk.json")){
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(serviceAccount)
                    .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
            googleCredentials.refreshIfExpired();
            return googleCredentials.getAccessToken().getTokenValue();
        }
    }
}

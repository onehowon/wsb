package com.ebiz.wsb.domain.notification.application;

import com.ebiz.wsb.domain.alert.application.AlertService;
import com.ebiz.wsb.domain.alert.entity.Alert;
import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.repository.GuardianRepository;
import com.ebiz.wsb.domain.notification.dto.AndroidNotificationDTO;
import com.ebiz.wsb.domain.notification.dto.FcmMessage;
import com.ebiz.wsb.domain.notification.dto.FcmMessage.Notification;
import com.ebiz.wsb.domain.notification.dto.NotificationDTO;
import com.ebiz.wsb.domain.notification.dto.PushType;
import com.ebiz.wsb.domain.notification.entity.FcmToken;
import com.ebiz.wsb.domain.notification.entity.UserType;
import com.ebiz.wsb.domain.notification.exception.PushNotFoundException;
import com.ebiz.wsb.domain.notification.repository.FcmTokenRepository;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.parent.repository.ParentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.net.HttpHeaders;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.TopicManagementResponse;
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

import java.util.ArrayList;
import java.util.HashMap;
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
    private final ParentRepository parentRepository;
    private final GuardianRepository guardianRepository;
    private final OkHttpClient client = new OkHttpClient();

    @Value("${fcm.project.id}")
    private String PROJECT_ID;
    private static final String FCM_SEND_URL = "https://fcm.googleapis.com/v1/projects/%s/messages:send";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    public void sendPushMessage(Long receiverId, String title, String body, Map<String, String> data, String token, Alert.AlertCategory category)
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

        alertService.createAlert(receiverId, category, title, body);
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

    public void sendPushNotificationToGroup(Long groupId, String title, String body, PushType pushType) {

        List<Parent> parents = parentRepository.findByGroupId(groupId);
        List<Guardian> guardians = guardianRepository.findByGroupId(groupId);


        List<String> fcmTokens = new ArrayList<>();

        for (Parent parent : parents) {
            List<FcmToken> parentTokens = fcmTokenRepository.findByUserIdAndUserType(parent.getId(), UserType.PARENT);
            parentTokens.forEach(token -> fcmTokens.add(token.getToken()));
        }

        for (Guardian guardian : guardians) {
            List<FcmToken> guardianTokens = fcmTokenRepository.findByUserIdAndUserType(guardian.getId(), UserType.GUARDIAN);
            guardianTokens.forEach(token -> fcmTokens.add(token.getToken()));
        }

        Map<String, String> data = createPushData(pushType);
        Alert.AlertCategory alertCategory = mapPushTypeToAlertCategory(pushType);

        for (String token : fcmTokens) {
            Long userId = fcmTokenRepository.findByToken(token)
                    .map(FcmToken::getUserId)
                    .orElse(null);

            if (userId != null) {
                try {
                    alertService.createAlert(userId, alertCategory, title, body);

                    sendPushMessage(userId, title, body, data, token, alertCategory);

                } catch (IOException e) {
                    log.error("푸시 메시지 전송 실패: token={} / error: {}", token, e.getMessage());
                } catch (Exception e) {
                    log.error("Alert 저장 실패 또는 예외 발생: userId={}, error: {}", userId, e.getMessage());
                }
            } else {
                log.warn("유효하지 않은 토큰으로 알림 전송 시도: token={}", token);
            }
        }
    }

    public void sendPushNotifcationToGuardians(Long groupId, String title, String body, PushType pushType){
        List<Guardian> guardians = guardianRepository.findByGroupId(groupId);

        List<String> guardianTokens = new ArrayList<>();
        for(Guardian guardian : guardians){
            List<FcmToken> tokens = fcmTokenRepository.findByUserIdAndUserType(guardian.getId(), UserType.GUARDIAN);
            tokens.forEach(token -> guardianTokens.add(token.getToken()));
        }

        Map<String, String> data = createPushData(pushType);
        Alert.AlertCategory alertCategory = mapPushTypeToAlertCategory(pushType);

        for ( String token : guardianTokens){
            Long userId = fcmTokenRepository.findByToken(token)
                    .map(FcmToken::getUserId)
                    .orElse(null);

            if(userId != null){
                try{
                    alertService.createAlert(userId, alertCategory, title, body);
                    sendPushMessage(userId, title, body, data, token, alertCategory);
                } catch (IOException e){
                    log.error("푸시 메시지 전송 실패: token={} / error : {}", token, e.getMessage());
                } catch (Exception e) {
                    log.error("Alert 저장 실패 또는 예외 발생: userId={}, error: {}", userId, e.getMessage());
                }
            } else {
                log.warn("유효하지 않은 토큰으로 알림 전송 시도: token={}", token);
            }
        }
    }

    public void sendPushNotificationToParents(Long groupId, String title, String body, PushType pushType) {
        List<Parent> parents = parentRepository.findByGroupId(groupId);

        List<String> parentTokens = new ArrayList<>();

        for (Parent parent : parents) {
            List<FcmToken> tokens = fcmTokenRepository.findByUserIdAndUserType(parent.getId(), UserType.PARENT);
            tokens.forEach(token -> parentTokens.add(token.getToken()));
        }

        Map<String, String> data = createPushData(pushType);
        Alert.AlertCategory alertCategory = mapPushTypeToAlertCategory(pushType);

        for (String token : parentTokens) {
            Long userId = fcmTokenRepository.findByToken(token)
                    .map(FcmToken::getUserId)
                    .orElse(null);

            if (userId != null) {
                try {
                    alertService.createAlert(userId, alertCategory, title, body);
                    sendPushMessage(userId, title, body, data, token, alertCategory);
                } catch (IOException e) {
                    log.error("푸시 메시지 전송 실패: token={} / error: {}", token, e.getMessage());
                } catch (Exception e) {
                    log.error("Alert 저장 실패 또는 예외 발생: userId={}, error: {}", userId, e.getMessage());
                }
            } else {
                log.warn("유효하지 않은 토큰으로 알림 전송 시도: token={}", token);
            }
        }
    }


    public Map<String, String> createPushData(PushType pushType) {
        Map<String, String> data = new HashMap<>();

        switch (pushType) {
            case SCHOOL:
                data.put("title", "등하교 인증");
                data.put("body", "등하교 인증이 등록되었어요. 지금 확인해보세요");
                break;
            case POST:
                data.put("title", "새로운 공지사항");
                data.put("body", "지도사님이 새로운 공지사항을 작성했습니다.");
                break;
            case APP:
                data.put("title", "시스템 공지");
                data.put("body", "시스템 공지입니다.");
                break;
            case SCHEDULE:
                data.put("title", "새로운 스케줄");
                data.put("body", "새로운 스케줄이 등록되었어요. 지금 확인해보세요");
                break;
            case MESSAGE:
                data.put("title", "새로운 메시지");
                data.put("body", "새로운 메시지가 도착했어요. 지금 확인해보세요");
                break;
            case START_WORK:
                data.put("title", "운행 알림");
                data.put("body", "지도사님이 운행을 시작했어요.");
                break;
            case PICKUP:
                data.put("title", "픽업 알림");
                data.put("body", "지도사님이 우리 아이를 픽업했어요.");
                break;
            case END_WORK:
                data.put("title", "운행 종료 알림");
                data.put("body", "지도사님이 운행을 종료했어요.");
                break;
            default:
                data.put("title", "일반 공지");
                data.put("body", "일반 공지사항입니다.");
        }

        return data;
    }

    private Alert.AlertCategory mapPushTypeToAlertCategory(PushType pushType) {
        switch (pushType) {
            case SCHOOL:
                return Alert.AlertCategory.SCHOOL;
            case POST:
                return Alert.AlertCategory.POST;
            case APP:
                return Alert.AlertCategory.APP;
            case SCHEDULE:
                return Alert.AlertCategory.SCHEDULE;
            case MESSAGE:
                return Alert.AlertCategory.MESSAGE;
            case START_WORK:
                return Alert.AlertCategory.START_WORK;
            case PICKUP:
                return Alert.AlertCategory.PICKUP;
            case END_WORK:
                return Alert.AlertCategory.END_WORK;
            default:
                throw new PushNotFoundException("지원되지 않는 PushType입니다.");
        }
    }


    // 테스트용 메서드
    public void sendMessageToToken(Long userId, String title, String body, Map<String, String> data, String token, Alert.AlertCategory category) throws IOException {
        sendPushMessage(userId, title, body, data, token, category);
    }
}

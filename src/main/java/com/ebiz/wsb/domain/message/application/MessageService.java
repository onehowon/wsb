package com.ebiz.wsb.domain.message.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.exception.GroupNotFoundException;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.message.dto.MessageDTO;
import com.ebiz.wsb.domain.message.entity.Message;
import com.ebiz.wsb.domain.message.entity.MessageRecipient;
import com.ebiz.wsb.domain.message.exception.MessageAccessException;
import com.ebiz.wsb.domain.message.repository.MessageRecipientRepository;
import com.ebiz.wsb.domain.message.repository.MessageRepository;
import com.ebiz.wsb.domain.notification.application.PushNotificationService;
import com.ebiz.wsb.domain.notification.dto.PushType;
import com.ebiz.wsb.domain.parent.dto.ParentDTO;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.parent.exception.ParentNotFoundException;
import com.ebiz.wsb.domain.student.entity.Student;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageRecipientRepository messageRecipientRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final PushNotificationService pushNotificationService;

    @Transactional
    public void sendMessage(String content) {
        // 현재 사용자 정보(인증 객체)로 학부모 여부 확인
        Object userByContextHolder = userDetailsService.getUserByContextHolder();
        if(userByContextHolder instanceof Parent) {
            Parent parent = (Parent) userByContextHolder;

            Group group = parent.getGroup();
            if (group == null) {
                throw new GroupNotFoundException("부모가 속한 그룹이 없습니다.");
            }

            // 메시지 생성 및 저장
            Message message = Message.builder()
                    .group(group)
                    .parent(parent)
                    .content(content)
                    .transferredAt(LocalDateTime.now())
                    .isRead(false)
                    .build();

            messageRepository.save(message);

            // 메시지 보낼 때, 인솔자에게 메시지 푸시알림 보내기
            Map<String, String> pushData = pushNotificationService.createPushData(PushType.MESSAGE);

            // title 내용에 학생 이름 삽입
            String titleWithStudentNames = String.format(
                    pushData.get("title"),
                    parent.getStudents().stream()
                            .map(Student::getName)
                            .collect(Collectors.joining(", "))
            );

            // body 내용에 메시지 내용 삽입
            String bodyWithContent = String.format(pushData.get("body"), content);

            pushData.put("title", titleWithStudentNames);
            pushData.put("body", bodyWithContent);

            pushNotificationService.sendPushNotifcationToGuardians(group.getId(), pushData.get("title"), pushData.get("body"), PushType.MESSAGE);

            // 해당 그룹의 모든 인솔자에게 메시지 보내기
            List<Guardian> guardians = group.getGuardians();
            for(Guardian guardian : guardians) {
                MessageRecipient recipient = MessageRecipient.builder()
                        .guardian(guardian)
                        .message(message)
                        .createdAt(LocalDateTime.now())
                        .build();

                messageRecipientRepository.save(recipient);
            }
        } else {
            throw new ParentNotFoundException("현재 사용자는 학부모가 아닙니다.");
        }
    }

    @Transactional
    public List<MessageDTO> getMessagesForGuardian() {
        Object userByContextHolder = userDetailsService.getUserByContextHolder();
        if (userByContextHolder instanceof Guardian) {
            Guardian guardian = (Guardian) userByContextHolder;
            List<MessageRecipient> recipients = messageRecipientRepository.findByGuardianId(guardian.getId());

            if (recipients.isEmpty()) {
                return Collections.emptyList();
            }

            recipients.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));

            List<Message> unreadMessages = new ArrayList<>();
            List<MessageDTO> messages = recipients.stream()
                    .map(recipient -> {
                        Message message = recipient.getMessage();

                        if (!message.isRead()) {
                            unreadMessages.add(message); // 비동기적으로 업데이트할 메시지 추가
                        }

                        return MessageDTO.builder()
                                .messageId(message.getMessageId())
                                .parent(toParentDTO(message.getParent()))
                                .content(message.getContent())
                                .transferredAt(message.getTransferredAt())
                                .isRead(message.isRead())
                                .build();
                    })
                    .collect(Collectors.toList());

            // 비동기 메서드를 통해 읽음 상태를 업데이트
            markMessagesAsReadAsync(unreadMessages);

            return messages;
        } else {
            throw new MessageAccessException("지도사님이 아니면 메시지를 확인할 수 없습니다");
        }
    }

    public List<MessageDTO> getMessagesForGuardianOne() {
        Object userByContextHolder = userDetailsService.getUserByContextHolder();
        if(userByContextHolder instanceof Guardian) {
            Guardian guardian = (Guardian) userByContextHolder;

            List<MessageRecipient> recipients = messageRecipientRepository.findByGuardianId(guardian.getId());

            // 메시지가 없는 경우 빈 리스트 반환
            if (recipients.isEmpty()) {
                return Collections.emptyList();
            }

            // createdAt 기준으로 내림차순 정렬
            recipients.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));

            // 최신의 메시지 하나 추출
            MessageRecipient recentMessageRecipient = recipients.get(0);

            MessageDTO messageDTO = MessageDTO.builder()
                    .messageId(recentMessageRecipient.getMessage().getMessageId())
                    .parent(toParentDTO(recentMessageRecipient.getMessage().getParent()))
                    .content(recentMessageRecipient.getMessage().getContent())
                    .transferredAt(recentMessageRecipient.getMessage().getTransferredAt())
                    .isRead(recentMessageRecipient.getMessage().isRead())
                    .build();

            return Collections.singletonList(messageDTO); // 단일 메시지를 리스트로 반환
        }
        throw new MessageAccessException("지도사님이 아니면 메시지를 확인할 수 없습니다");
    }

    @Async
    public void markMessagesAsReadAsync(List<Message> messages) {
        messages.forEach(message -> {
            message.setRead(true);   // 메시지의 읽음 상태를 true로 설정
            messageRepository.save(message); // 변경 사항을 저장
        });
    }


    private ParentDTO toParentDTO(Parent parent) {
        return ParentDTO.builder()
                .id(parent.getId())
                .name(parent.getName())
                .imagePath(parent.getImagePath())
                .build();
    }
}

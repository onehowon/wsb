package com.ebiz.wsb.domain.message.application;

import com.ebiz.wsb.domain.alert.entity.Alert;
import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.group.dto.GroupDTO;
import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.exception.GroupNotFoundException;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.exception.GuardianNotFoundException;
import com.ebiz.wsb.domain.guardian.repository.GuardianRepository;
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
import com.ebiz.wsb.domain.parent.repository.ParentRepository;
import com.ebiz.wsb.domain.student.entity.Student;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
                        .build();
                messageRecipientRepository.save(recipient);
            }
        } else {
            throw new ParentNotFoundException("현재 사용자는 학부모가 아닙니다.");
        }
    }

    public List<MessageDTO> getMessagesForGuardian() {
        // 현재 사용자 정보(인증 객체)로 학부모 여부 확인
        Object userByContextHolder = userDetailsService.getUserByContextHolder();
        if(userByContextHolder instanceof Guardian) {
            Guardian guardian = (Guardian) userByContextHolder;
            List<MessageRecipient> recipients = messageRecipientRepository.findByGuardianId(guardian.getId());

            // 메시지가 없는 경우 빈 리스트 반환
            if (recipients.isEmpty()) {
                return Collections.emptyList();
            }

            // MessageRecipient 엔티티를 MessageDTO로 변환
            return recipients.stream()
                    .map(recipient -> MessageDTO.builder()
                            .messageId(recipient.getMessage().getMessageId())
                            .parent(toParentDTO(recipient.getMessage().getParent()))
                            .content(recipient.getMessage().getContent())
                            .transferredAt(recipient.getMessage().getTransferredAt())
                            .isRead(recipient.isRead())
                            .build())
                    .collect(Collectors.toList());
        } else {
            throw new GuardianNotFoundException("현재 사용자는 지도사가 아닙니다.");
        }
    }

    private ParentDTO toParentDTO(Parent parent) {
        return ParentDTO.builder()
                .id(parent.getId())
                .name(parent.getName())
                .imagePath(parent.getImagePath())
                .build();
    }

    @Transactional
    public void markMessageAsRead(Long messageId) {
        // 현재 사용자 정보(인증 객체)로 인솔자 여부 확인
        Object userByContextHolder = userDetailsService.getUserByContextHolder();
        if (userByContextHolder instanceof Guardian) {
            Guardian guardian = (Guardian) userByContextHolder;

            // 특정 메시지 수신자를 조회하여 읽음 상태를 업데이트
            MessageRecipient recipient = messageRecipientRepository
                    .findByMessage_messageIdAndGuardian_Id(messageId, guardian.getId())
                    .orElseThrow(() -> new MessageAccessException("해당 메시지를 찾을 수 없습니다."));

            // 읽음 상태로 변경
            recipient.markAsRead();
            messageRecipientRepository.save(recipient);
        } else {
            throw new GuardianNotFoundException("현재 사용자는 지도사가 아닙니다.");
        }
    }

}

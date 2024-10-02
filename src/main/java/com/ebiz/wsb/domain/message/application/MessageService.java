package com.ebiz.wsb.domain.message.application;

import com.ebiz.wsb.domain.group.dto.GroupDTO;
import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.exception.GroupNotFoundException;
import com.ebiz.wsb.domain.guardian.dto.GuardianDTO;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.exception.GuardianNotFoundException;
import com.ebiz.wsb.domain.guardian.repository.GuardianRepository;
import com.ebiz.wsb.domain.message.dto.MessageDTO;
import com.ebiz.wsb.domain.message.entity.Message;
import com.ebiz.wsb.domain.message.entity.MessageRecipient;
import com.ebiz.wsb.domain.message.repository.MessageRecipientRepository;
import com.ebiz.wsb.domain.message.repository.MessageRepository;
import com.ebiz.wsb.domain.parent.dto.ParentDTO;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.parent.exception.ParentNotFoundException;
import com.ebiz.wsb.domain.parent.repository.ParentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.Guard;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final ParentRepository parentRepository;
    private final MessageRepository messageRepository;
    private final MessageRecipientRepository messageRecipientRepository;
    private final GuardianRepository guardianRepository;

    @Transactional
    public MessageDTO sendMessage(Long parentId, String content) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ParentNotFoundException("부모 정보를 찾을 수 없습니다."));


        Group group = parent.getGroup();
        if (group == null) {
            throw new GroupNotFoundException("부모가 속한 그룹이 없습니다.");
        }

        Message message = Message.builder()
                .group(group)
                .parent(parent)
                .content(content)
                .transferredAt(LocalDateTime.now())
                .build();

        messageRepository.save(message);

        List<Guardian> guardians = group.getGuardians();
        for(Guardian guardian : guardians) {
            MessageRecipient recipient = MessageRecipient.builder()
                    .guardian(guardian)
                    .message(message)
                    .build();

            messageRecipientRepository.save(recipient);
        }

        return MessageDTO.builder()
                .messageId(message.getMessageId())
                .content(message.getContent())
                .transferredAt(message.getTransferredAt())
                .build();
    }

    public List<MessageDTO> getMessagesForGuardian(Long guardianId) {

        Guardian guardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> new GuardianNotFoundException("인솔자를 찾을 수 없습니다."));

        List<MessageRecipient> recipients = messageRecipientRepository.findByGuardianId(guardianId);

        if (recipients.isEmpty()) {
            return Collections.emptyList();
        }

        return recipients.stream()
                .map(recipient -> MessageDTO.builder()
                        .messageId(recipient.getMessage().getMessageId())
                        .group(toGroupDTO(recipient.getMessage().getGroup()))
                        .parent(toParentDTO(recipient.getMessage().getParent()))
                        .content(recipient.getMessage().getContent())
                        .transferredAt(recipient.getMessage().getTransferredAt())
                        .build())
                .collect(Collectors.toList());

    }

    private GroupDTO toGroupDTO(Group group) {
        return GroupDTO.builder()
                .id(group.getId())
                .groupName(group.getGroupName())
                .build();
    }

    private ParentDTO toParentDTO(Parent parent) {
        return ParentDTO.builder()
                .id(parent.getId())
                .name(parent.getName())
                .build();
    }
}

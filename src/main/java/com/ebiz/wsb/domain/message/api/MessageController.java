package com.ebiz.wsb.domain.message.api;

import com.ebiz.wsb.domain.guardian.exception.GuardianNotFoundException;
import com.ebiz.wsb.domain.message.application.MessageService;
import com.ebiz.wsb.domain.message.dto.MessageDTO;
import com.ebiz.wsb.global.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send/{parentId}")
    public ResponseEntity<MessageDTO> sendMessage(
            @PathVariable Long parentId,
            @RequestBody MessageDTO messageDTO) {

        MessageDTO message = messageService.sendMessage(parentId, messageDTO.getContent());
        return ResponseEntity.ok(message);


    }

    @GetMapping("/received/{guardianId}")
    public ResponseEntity<?> getMessagesForGuardian(@PathVariable Long guardianId) {
        List<MessageDTO> messagesForGuardian = messageService.getMessagesForGuardian(guardianId);
        if(messagesForGuardian.isEmpty()){
            return ResponseEntity.ok(BaseResponse.builder()
                    .message("받은 메시지가 존재하지 않습니다.")
                    .build());
        }
        return ResponseEntity.ok(BaseResponse.builder()
                .data(messagesForGuardian)
                .build());
    }
}

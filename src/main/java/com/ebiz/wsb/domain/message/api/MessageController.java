package com.ebiz.wsb.domain.message.api;

import com.ebiz.wsb.domain.guardian.exception.GuardianNotFoundException;
import com.ebiz.wsb.domain.message.application.MessageService;
import com.ebiz.wsb.domain.message.dto.MessageDTO;
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
            @RequestParam String content) {

        MessageDTO messageDTO = messageService.sendMessage(parentId, content);
        return ResponseEntity.ok(messageDTO);


    }

    @GetMapping("/received/{guardianId}")
    public ResponseEntity<?> getMessagesForGuardian(@PathVariable Long guardianId) {
        try {
            List<MessageDTO> messagesForGuardian = messageService.getMessagesForGuardian(guardianId);

            if (messagesForGuardian.isEmpty()) {
                return ResponseEntity.ok("받은 메시지가 없습니다.");
            }

            return ResponseEntity.ok(messagesForGuardian);
        } catch (GuardianNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }

    }
}

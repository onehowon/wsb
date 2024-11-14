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

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestParam String content) {
        messageService.sendMessage(content);
        return ResponseEntity.ok("메시지가 성공적으로 전달되었습니다.");
    }

    @GetMapping("/received")
    public ResponseEntity<?> getMessagesForGuardian() {
        try {
            List<MessageDTO> messagesForGuardian = messageService.getMessagesForGuardian();

            if (messagesForGuardian.isEmpty()) {
                return ResponseEntity.ok("받은 메시지가 없습니다.");
            }
            return ResponseEntity.ok(messagesForGuardian);
        } catch (GuardianNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/received/one")
    public ResponseEntity<?> getMessagesForGuardianOne() {
        List<MessageDTO> messagesForGuardianOne = messageService.getMessagesForGuardianOne();
        return ResponseEntity.ok(messagesForGuardianOne);
    }

}

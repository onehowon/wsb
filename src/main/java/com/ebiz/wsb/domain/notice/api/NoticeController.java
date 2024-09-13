package com.ebiz.wsb.domain.notice.api;

import com.ebiz.wsb.domain.notice.application.NoticeService;
import com.ebiz.wsb.domain.notice.dto.NoticeDTO;
import com.ebiz.wsb.domain.notice.entity.Notice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 모든 공지사항 가져오기
    @GetMapping
    public ResponseEntity<List<NoticeDTO>> getAllNotices(){
        List<NoticeDTO> notices = noticeService.getAllNotices();
        return ResponseEntity.ok(notices);
    }

    // 특정 공지사항만 가져오기
    @GetMapping("/{id}")
    public ResponseEntity<NoticeDTO> getNoticeById(@PathVariable Long id){
        NoticeDTO noticeDTO = noticeService.getNoticeById(id);
        if (noticeDTO == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(noticeDTO);
    }

    @PostMapping
    public ResponseEntity<NoticeDTO> createNotice(@RequestBody NoticeDTO newNoticeDTO) {
        NoticeDTO createdNotice = noticeService.createNotice(newNoticeDTO);
        return new ResponseEntity<>(createdNotice, HttpStatus.CREATED);
    }

    // 기존 공지사항 수정
    @PutMapping("/{id}")
    public ResponseEntity<NoticeDTO> updateNotice(@PathVariable Long id, @RequestBody NoticeDTO updatedNoticeDTO) {
        NoticeDTO updated = noticeService.updateNotice(id, updatedNoticeDTO);
        return ResponseEntity.ok(updated);
    }

    // 공지사항 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

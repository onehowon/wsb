package com.ebiz.wsb.domain.notice.api;

import com.ebiz.wsb.domain.notice.application.GroupNoticeService;
import com.ebiz.wsb.domain.notice.dto.GroupNoticeDTO;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/group-notices")
@AllArgsConstructor
public class GroupNoticeController {

    private final GroupNoticeService groupNoticeService;
    @GetMapping
    public ResponseEntity<Page<GroupNoticeDTO>> getAllGroupNotices(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<GroupNoticeDTO> groupNotices = groupNoticeService.getAllGroupNotices(pageable);
        return ResponseEntity.ok(groupNotices);
    }

    @GetMapping("/{groupNoticeId}")
    public ResponseEntity<GroupNoticeDTO> getGroupNoticeById(@PathVariable Long groupNoticeId) {
        GroupNoticeDTO groupNotice = groupNoticeService.getGroupNoticeById(groupNoticeId);
        return ResponseEntity.ok(groupNotice);
    }

    @PostMapping
    public ResponseEntity<GroupNoticeDTO> createGroupNotice(
            @RequestParam("noticeType") String noticeType,
            @RequestParam("content") String content,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            Authentication authentication
    ) {
        GroupNoticeDTO createdGroupNotice = groupNoticeService.createGroupNotice(noticeType, content, photo, authentication);
        return new ResponseEntity<>(createdGroupNotice, HttpStatus.CREATED);
    }

    @PutMapping("/{groupNoticeId}")
    public ResponseEntity<GroupNoticeDTO> updateGroupNotice(
            @PathVariable Long groupNoticeId,
            @RequestParam("content") String content,
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) {
        GroupNoticeDTO updatedNotice = groupNoticeService.updateGroupNotice(groupNoticeId, content, photo);
        return ResponseEntity.ok(updatedNotice);
    }

    @DeleteMapping("/{groupNoticeId}")
    public ResponseEntity<Void> deleteGroupNotice(@PathVariable Long groupNoticeId) {
        groupNoticeService.deleteGroupNotice(groupNoticeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

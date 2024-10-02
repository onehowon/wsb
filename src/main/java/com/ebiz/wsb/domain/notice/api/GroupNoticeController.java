package com.ebiz.wsb.domain.notice.api;

import com.ebiz.wsb.domain.notice.application.GroupNoticeService;
import com.ebiz.wsb.domain.notice.dto.GroupNoticeDTO;
import com.ebiz.wsb.domain.notice.entity.GroupNotice;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/group-notices")
@AllArgsConstructor
public class GroupNoticeController {

    private final GroupNoticeService groupNoticeService;
    @GetMapping
    public ResponseEntity<List<GroupNoticeDTO>> getAllGroupNotices() {
        List<GroupNoticeDTO> groupNotices = groupNoticeService.getAllGroupNotices();
        return ResponseEntity.ok(groupNotices);
    }

    @GetMapping("/{groupNoticeId}")
    public ResponseEntity<GroupNoticeDTO> getGroupNoticeById(@PathVariable Long groupNoticeId) {
        GroupNoticeDTO groupNotice = groupNoticeService.getGroupNoticeById(groupNoticeId);
        return ResponseEntity.ok(groupNotice);
    }

    @PostMapping
    public ResponseEntity<GroupNoticeDTO> createGroupNotice(@RequestBody GroupNoticeDTO groupNoticeDTO) {
        GroupNoticeDTO createdGroupNotice = groupNoticeService.createGroupNotice(groupNoticeDTO);
        return new ResponseEntity<>(createdGroupNotice, HttpStatus.CREATED);
    }

    @PutMapping("/{groupNoticeId}")
    public ResponseEntity<GroupNoticeDTO> updateGroupNotice(
            @PathVariable Long groupNoticeId,
            @RequestBody GroupNoticeDTO updatedGroupNoticeDTO) {
        GroupNoticeDTO updatedNotice = groupNoticeService.updateGroupNotice(groupNoticeId, updatedGroupNoticeDTO);
        return ResponseEntity.ok(updatedNotice);
    }

    @DeleteMapping("/{groupNoticeId}")
    public ResponseEntity<Void> deleteGroupNotice(@PathVariable Long groupNoticeId) {
        groupNoticeService.deleteGroupNotice(groupNoticeId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

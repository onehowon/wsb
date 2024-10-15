package com.ebiz.wsb.domain.group.api;

import com.ebiz.wsb.domain.group.application.GroupService;
import com.ebiz.wsb.domain.group.dto.GroupDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    // 출근하기 (인솔자가 "출근하기" 누르면 누른 인솔자 ID로 그룹 출근 상태 업데이트)
    @PostMapping("/{groupId}/start-guide")
    public ResponseEntity<GroupDTO> startGuide(@PathVariable Long groupId) {
        GroupDTO groupDTO = groupService.startGuide(groupId);
        return ResponseEntity.ok(groupDTO);
    }

    // 퇴근하기 (인솔자가 "퇴근하기" 누르면 그룹 출근 상태 종료)
    @PostMapping("/{groupId}/stop-guide")
    public ResponseEntity<GroupDTO> stopGuide(@PathVariable Long groupId) {
        GroupDTO groupDTO = groupService.stopGuide(groupId);
        return ResponseEntity.ok(groupDTO);
    }
}

package com.ebiz.wsb.domain.student.api;

import com.ebiz.wsb.domain.student.dto.GroupStudentAssignRequest;
import com.ebiz.wsb.domain.student.service.GroupStudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/group-student")
@RequiredArgsConstructor
public class GroupStudentController {

    private final GroupStudentService groupStudentService;

    @PostMapping("/assign")
    public ResponseEntity<String> assignStudentToGroup(@RequestBody GroupStudentAssignRequest request) {
        groupStudentService.assignStudentToGroup(request);
        return ResponseEntity.ok("학생이 그룹에 성공적으로 할당되었습니다.");
    }
}
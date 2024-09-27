package com.ebiz.wsb.domain.route.api;

import com.ebiz.wsb.domain.route.application.GroupRouteService;
import com.ebiz.wsb.domain.route.dto.GroupRouteAssignRequest;
import com.ebiz.wsb.domain.student.dto.GroupStudentAssignRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group-route")
public class GroupRouteController {

    private final GroupRouteService groupRouteService;

    @PostMapping("/assign")
    public ResponseEntity<String> assignRouteToGroup(@RequestBody @Valid GroupRouteAssignRequest groupRouteAssignRequest){
        Long groupId = groupRouteAssignRequest.getGroupId();
        Long routeId = groupRouteAssignRequest.getRouteId();

        groupRouteService.assignRouteToGroup(groupId, routeId);
        return ResponseEntity.ok("경유지가 그룹에 성공적으로 할당되었습니다.");
    }
}
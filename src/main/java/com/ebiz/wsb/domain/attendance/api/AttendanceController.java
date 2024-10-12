package com.ebiz.wsb.domain.attendance.api;

import com.ebiz.wsb.domain.attendance.application.AttendanceService;
import com.ebiz.wsb.domain.attendance.dto.AttendanceUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AttendanceController {

    private final AttendanceService attendanceService;

    @MessageMapping("/group/{groupId}")
    public void updateAttendance(@Payload AttendanceUpdateRequest request, @DestinationVariable Long groupId) {
        attendanceService.updateAttendance(request.getStudentId(), request.getAttendanceStatus(), groupId);
    }
}

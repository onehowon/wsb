package com.ebiz.wsb.domain.attendance.api;

import com.ebiz.wsb.domain.attendance.application.AttendanceService;
import com.ebiz.wsb.domain.attendance.dto.AttendanceDTO;
import com.ebiz.wsb.domain.attendance.dto.AttendanceStatusDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AttendanceWebSocketController {
//    private final SimpMessagingTemplate messagingTemplate;
//    private final AttendanceService attendanceService;
//
//
//    @MessageMapping("/attendance/update")
//    public void updateAttendanceWebSocket(Long attendanceId, AttendanceStatusDTO attendanceStatusDTO) {
//
//        AttendanceDTO updatedAttendance = attendanceService.updateAttendance(attendanceId, attendanceStatusDTO);
//
//        log.info("출석 상태 업데이트: 학생 ID={}, 상태={}", updatedAttendance.getStudentId(), updatedAttendance.getStatus());
//
//        messagingTemplate.convertAndSend("/sub/attendance/updates", updatedAttendance);
//    }
}
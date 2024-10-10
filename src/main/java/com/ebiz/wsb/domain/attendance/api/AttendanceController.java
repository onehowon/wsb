package com.ebiz.wsb.domain.attendance.api;

import com.ebiz.wsb.domain.attendance.application.AttendanceService;
import com.ebiz.wsb.domain.attendance.dto.AttendanceUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/update")
    public ResponseEntity<String> updateAttendance(@RequestBody AttendanceUpdateRequest request) {
        attendanceService.updateAttendance(request.getStudentId(), request.getAttendanceStatus());
        return ResponseEntity.ok("출석 상태가 업데이트되었습니다.");
    }

}

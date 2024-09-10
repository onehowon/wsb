package com.ebiz.wsb.domain.attendance.api;

import com.ebiz.wsb.domain.attendance.application.AttendanceService;
import com.ebiz.wsb.domain.attendance.dto.AttendanceDTO;
import com.ebiz.wsb.domain.attendance.dto.AttendanceStatusDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService){
        this.attendanceService = attendanceService;
    }

    @PutMapping("/{attendanceId}")
    public ResponseEntity<AttendanceDTO> updateAttendance(@PathVariable Long attendanceId,
                                                          @RequestBody AttendanceStatusDTO attendanceStatusDTO){
        AttendanceDTO updateAttendance = attendanceService.updateAttendance(attendanceId, attendanceStatusDTO);
        return ResponseEntity.ok(updateAttendance);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceByStudentId(@PathVariable Long studentId){
        List<AttendanceDTO> attendanceList = attendanceService.getAttendanceByStudentId(studentId);
        return ResponseEntity.ok(attendanceList);
    }
}

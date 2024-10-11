package com.ebiz.wsb.domain.attendance.dto;

import com.ebiz.wsb.domain.attendance.entity.AttendanceStatus;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AttendanceDTO {
    private Long attendanceId;
    private Long studentId;
    private Long waypointId;
    private AttendanceStatus attendanceStatus;
    private LocalDate attendanceDate;
    private Boolean parentNoticeAbsence;
}

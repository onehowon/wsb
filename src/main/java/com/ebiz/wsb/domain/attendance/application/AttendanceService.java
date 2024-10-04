package com.ebiz.wsb.domain.attendance.application;

import com.ebiz.wsb.domain.attendance.dto.AttendanceDTO;
import com.ebiz.wsb.domain.attendance.dto.AttendanceStatusDTO;
import com.ebiz.wsb.domain.attendance.entity.Attendance;
import com.ebiz.wsb.domain.attendance.entity.AttendanceStatus;
import com.ebiz.wsb.domain.attendance.exception.AttendanceNotFoundException;
import com.ebiz.wsb.domain.attendance.exception.StatusNotFoundException;
import com.ebiz.wsb.domain.attendance.repository.AttendanceRepository;
import com.ebiz.wsb.domain.attendance.repository.AttendanceStatusRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceStatusRepository attendanceStatusRepository;

    public AttendanceDTO updateAttendance(Long attendanceId, AttendanceStatusDTO updatedAttendanceDTO) {
        return attendanceRepository.findById(attendanceId)
                .map(existingAttendance -> {
                    AttendanceStatus status = attendanceStatusRepository.findByName(updatedAttendanceDTO.getStatus())
                            .orElseThrow(() -> new StatusNotFoundException("해당 출결 상태를 찾을 수 없습니다: " + updatedAttendanceDTO.getStatus()));

                    Timestamp checkTime = convertToTimestamp(
                            updatedAttendanceDTO.getCheckTime() != null
                                    ? updatedAttendanceDTO.getCheckTime()
                                    : LocalDateTime.now()
                    );

                    Attendance updatedAttendance = Attendance.builder()
                            .attendanceId(existingAttendance.getAttendanceId())
                            .studentId(existingAttendance.getStudentId()) // 학생 정보 유지
                            .attendanceDate(existingAttendance.getAttendanceDate()) // 기존 출결 날짜 유지
                            .status(status)
                            .checkTime(checkTime)
                            .build();

                    Attendance savedAttendance = attendanceRepository.save(updatedAttendance);

                    return convertToDTO(savedAttendance);
                })
                .orElseThrow(() -> new AttendanceNotFoundException(attendanceId));
    }

    public List<AttendanceDTO> getAttendanceByStudentId(Long studentId) {
        List<Attendance> attendanceList = attendanceRepository.findByStudentId(studentId);
        return attendanceList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    private AttendanceDTO convertToDTO(Attendance attendance) {
        return AttendanceDTO.builder()
                .attendanceId(attendance.getAttendanceId())
                .studentId(attendance.getStudentId())
                .status(attendance.getStatus().getName())
                .attendanceDate(attendance.getAttendanceDate())
                .checkTime(attendance.getCheckTime())
                .build();
    }

    public Timestamp convertToTimestamp(LocalDateTime localDateTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        return Timestamp.from(zdt.toInstant());
    }
}

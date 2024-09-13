package com.ebiz.wsb.domain.attendance.application;

import com.ebiz.wsb.domain.attendance.dto.AttendanceDTO;
import com.ebiz.wsb.domain.attendance.dto.AttendanceStatusDTO;
import com.ebiz.wsb.domain.attendance.entity.Attendance;
import com.ebiz.wsb.domain.attendance.entity.AttendanceStatus;
import com.ebiz.wsb.domain.attendance.exception.AttendanceNotFoundException;
import com.ebiz.wsb.domain.attendance.exception.StatusNotFoundException;
import com.ebiz.wsb.domain.attendance.repository.AttendanceRepository;
import com.ebiz.wsb.domain.attendance.repository.AttendanceStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private AttendanceStatusRepository attendanceStatusRepository;

    // 출결 업데이트 (인솔자가 특정 학생의 출석 상태를 인증하는 상황)
    public AttendanceDTO updateAttendance(Long attendanceId, AttendanceStatusDTO updatedAttendanceDTO) {
        return attendanceRepository.findById(attendanceId)
                .map(existingAttendance -> {
                    // status 업데이트를 위해 AttendanceStatus를 조회
                    AttendanceStatus status = attendanceStatusRepository.findByName(updatedAttendanceDTO.getStatus())
                            .orElseThrow(() -> new StatusNotFoundException("해당 출결 상태를 찾을 수 없습니다: " + updatedAttendanceDTO.getStatus()));

                    // LocalDateTime을 Timestamp로 변환
                    Timestamp checkTime = convertToTimestamp(
                            updatedAttendanceDTO.getCheckTime() != null
                                    ? updatedAttendanceDTO.getCheckTime()
                                    : LocalDateTime.now()
                    );

                    // 출결 정보 업데이트
                    Attendance updatedAttendance = Attendance.builder()
                            .attendanceId(existingAttendance.getAttendanceId())
                            .studentId(existingAttendance.getStudentId()) // 학생 정보 유지
                            .attendanceDate(existingAttendance.getAttendanceDate()) // 기존 출결 날짜 유지
                            .status(status) // 상태 업데이트
                            .checkTime(checkTime) // 체크 시간 업데이트
                            .build();

                    Attendance savedAttendance = attendanceRepository.save(updatedAttendance);

                    // 변환된 DTO 반환
                    return convertToDTO(savedAttendance);
                })
                .orElseThrow(() -> new AttendanceNotFoundException(attendanceId));
    }
    // 학생별 출결 정보 조회
    public List<AttendanceDTO> getAttendanceByStudentId(Long studentId) {
        List<Attendance> attendanceList = attendanceRepository.findByStudentId(studentId);
        return attendanceList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Attendance 엔티티를 AttendanceDTO로 변환하는 메서드
    private AttendanceDTO convertToDTO(Attendance attendance) {
        return AttendanceDTO.builder()
                .attendanceId(attendance.getAttendanceId())
                .studentId(attendance.getStudentId())
                .status(attendance.getStatus().getName()) // 상태의 이름을 반환
                .attendanceDate(attendance.getAttendanceDate())
                .checkTime(attendance.getCheckTime())
                .build();
    }

    // LocalDateTime을 Timestamp로 변환하는 메서드
    public Timestamp convertToTimestamp(LocalDateTime localDateTime) {
        ZoneId zoneId = ZoneId.systemDefault();  // 시스템 기본 시간대 사용
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        return Timestamp.from(zdt.toInstant());
    }
}

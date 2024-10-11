package com.ebiz.wsb.domain.attendance.application;

import com.ebiz.wsb.domain.attendance.dto.AttendanceDTO;
import com.ebiz.wsb.domain.attendance.entity.Attendance;
import com.ebiz.wsb.domain.attendance.entity.AttendanceStatus;
import com.ebiz.wsb.domain.attendance.repository.AttendanceRepository;
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.student.exception.StudentNotFoundException;
import com.ebiz.wsb.domain.student.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final SimpMessagingTemplate template;

    @Transactional
    public void updateAttendance(Long studentId, AttendanceStatus attendanceStatus, Long groupId) {
        LocalDate today = LocalDate.now();
        log.info(studentId.toString());
        log.info(attendanceStatus.toString());
        log.info(groupId.toString());

        // 학생 정보 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("학생 정보를 찾을 수 없습니다"));

        log.info(student.toString());

        // 오늘 날짜에 해당하는 출석 정보 조회
        Attendance attendance = attendanceRepository.findByStudentAndAttendanceDate(student, today)
                .orElseGet(() -> Attendance.builder()
                        .student(student)
                        .waypoint(student.getWaypoint())
                        .attendanceDate(today)
                        .attendanceStatus(AttendanceStatus.UNCONFIRMED)
                        .build());

        // 새로운 상태로 출석 정보 업데이트 (객체 재생성)
        Attendance updatedAttendance = attendance.toBuilder()
                .attendanceStatus(attendanceStatus)
                .build();
        log.info(updatedAttendance.toString());

        Attendance save = attendanceRepository.save(updatedAttendance);

        AttendanceDTO attendanceDTO = AttendanceDTO.builder()
                .attendanceId(save.getAttendanceId())
                .studentId(save.getStudent().getStudentId())
                .waypointId(save.getWaypoint().getId())
                .attendanceDate(save.getAttendanceDate())
                .attendanceStatus(save.getAttendanceStatus())
                .build();

        log.info(attendanceDTO.toString());

        // 출석 상태 업데이트 웹소캣으로 인솔자들에게 알림
        template.convertAndSend("/sub/group/" + groupId, attendanceDTO);
    }
}

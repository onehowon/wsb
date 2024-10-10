package com.ebiz.wsb.domain.attendance.application;

import com.ebiz.wsb.domain.attendance.entity.Attendance;
import com.ebiz.wsb.domain.attendance.entity.AttendanceStatus;
import com.ebiz.wsb.domain.attendance.repository.AttendanceRepository;
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.student.exception.StudentNotFoundException;
import com.ebiz.wsb.domain.student.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void updateAttendance(Long studentId, Long groupId, AttendanceStatus newStatus) {
        LocalDate today = LocalDate.now();

        // 학생 정보 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("학생 정보를 찾을 수 없습니다"));

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
                .attendanceStatus(newStatus)
                .build();

        attendanceRepository.save(updatedAttendance);

        // 출석 상태 업데이트 웹소캣으로 인솔자들에게 알림
        sendAttendanceUpdateNotification(updatedAttendance, groupId);
    }

    private void sendAttendanceUpdateNotification(Attendance updatedAttendance, Long groupId) {
        // 변경된 출석 정보를 WebSocket 채널로 전송
        messagingTemplate.convertAndSend("/sub/group/" + groupId, updatedAttendance);
    }
}

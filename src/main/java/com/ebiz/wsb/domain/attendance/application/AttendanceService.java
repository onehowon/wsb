package com.ebiz.wsb.domain.attendance.application;

import com.ebiz.wsb.domain.attendance.dto.AttendanceDTO;
import com.ebiz.wsb.domain.attendance.entity.Attendance;
import com.ebiz.wsb.domain.attendance.entity.AttendanceStatus;
import com.ebiz.wsb.domain.attendance.repository.AttendanceRepository;
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.student.exception.StudentNotFoundException;
import com.ebiz.wsb.domain.student.repository.StudentRepository;
import com.ebiz.wsb.domain.waypoint.entity.Waypoint;
import com.ebiz.wsb.domain.waypoint.exception.WaypointNotFoundException;
import com.ebiz.wsb.domain.waypoint.repository.WaypointRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final SimpMessagingTemplate template;
    private final WaypointRepository waypointRepository;

    @Transactional
    public void updateAttendance(Long studentId, AttendanceStatus attendanceStatus, Long groupId) {

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

        // 경유지에도 현재 "출석완료"인 학생 수 반영
        Waypoint waypoint = waypointRepository.findById(attendance.getWaypoint().getId())
                .orElseThrow(() -> new WaypointNotFoundException("해당 경유지를 찾을 수 없습니다"));

        // 경유지에도 현재 "출석완료"인 학생 수 반영
        Waypoint updatedWaypoint = waypoint.toBuilder()  // 새롭게 빌드된 객체를 updatedWaypoint에 저장
                .currentCount(attendanceStatus == AttendanceStatus.PRESENT
                        ? waypoint.getCurrentCount() + 1
                        : waypoint.getCurrentCount() - 1)
                .build();

        waypointRepository.save(updatedWaypoint); // 새롭게 빌드된 객체를 저장


        // 새로운 상태로 출석 정보 업데이트
        Attendance updatedAttendance = attendance.toBuilder()
                .attendanceStatus(attendanceStatus)
                .build();

        Attendance save = attendanceRepository.save(updatedAttendance);


        AttendanceDTO attendanceDTO = AttendanceDTO.builder()
                .attendanceId(save.getAttendanceId())
                .studentId(save.getStudent().getStudentId())
                .waypointId(save.getWaypoint().getId())
                .attendanceDate(save.getAttendanceDate())
                .attendanceStatus(save.getAttendanceStatus())
                .build();


        template.convertAndSend("/sub/group/" + groupId, attendanceDTO);
    }

    @Transactional
    public void completeAttendance(Long waypointId) {
        // 경유지 정보 조회
        Waypoint waypoint = waypointRepository.findById(waypointId)
                .orElseThrow(() -> new WaypointNotFoundException("해당 경유지를 찾을 수 없습니다"));

        // 해당 경유지의 오늘자 출석 정보 조회
        List<Attendance> attendances = attendanceRepository.findByWaypoint_IdAndAttendanceDate(waypoint.getId(), LocalDate.now());
        System.out.println("AttendanceService.completeAttendance");
        log.info(attendances.toString());

        // 출석완료 버튼을 누르면, "미인증"인 학생을 "결석"으로 처리
        List<Attendance> updatedAttendances = attendances.stream()
                .map(attendance -> {
                    log.info(attendance.getAttendanceStatus().toString());
                    if (attendance.getAttendanceStatus() == AttendanceStatus.UNCONFIRMED) {
                        // 미인증 상태인 학생을 결석으로 변경
                        return attendance.toBuilder()
                                .attendanceStatus(AttendanceStatus.ABSENT)
                                .build();
                    }
                    return attendance; // 변경 없는 경우 기존 객체 반환
                })
                .collect(Collectors.toList());

        // 출석 완료 버튼 누를 때, "출석완료"인 학생만 count 하기 위한 변수
        int currentCount = (int) updatedAttendances.stream()
                .filter(attendance -> attendance.getAttendanceStatus() == AttendanceStatus.PRESENT)
                .count();

        // "미인증"을 "결석"으로 처리 후 저장
        attendanceRepository.saveAll(updatedAttendances);

        // 경유지에 대한 출석 완료 여부를 false -> true로 변경하고, 출석 학생 수 업데이트
        Waypoint updatedWaypoint = waypoint.toBuilder()
                .attendanceComplete(true)
                .currentCount(currentCount)
                .build();

        // 출석 여부를 변경한 경유지 자체를 업데이트 및 저장
        waypointRepository.save(updatedWaypoint);
    }
}

package com.ebiz.wsb.domain.attendance.application;

import com.ebiz.wsb.domain.attendance.dto.AttendanceDTO;
import com.ebiz.wsb.domain.attendance.entity.Attendance;
import com.ebiz.wsb.domain.attendance.entity.AttendanceStatus;
import com.ebiz.wsb.domain.attendance.repository.AttendanceRepository;
import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.parent.exception.ParentNotFoundException;
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.student.exception.StudentNotFoundException;
import com.ebiz.wsb.domain.student.repository.StudentRepository;
import com.ebiz.wsb.domain.waypoint.entity.Waypoint;
import com.ebiz.wsb.domain.waypoint.exception.WaypointNotFoundException;
import com.ebiz.wsb.domain.waypoint.repository.WaypointRepository;
import com.ebiz.wsb.global.dto.BaseResponse;
import com.fasterxml.jackson.databind.ser.Serializers;
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
    private final UserDetailsServiceImpl userDetailsService;

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

        // 이미 출석 상태가 동일한 경우 중복 처리를 방지
        if (attendance.getAttendanceStatus() == attendanceStatus) {
            // 상태가 동일하다면 아무 작업도 하지 않고 반환
            log.info("중복된 출석 상태 업데이트 요청이 감지되었습니다. 처리하지 않습니다.");
            return;
        }

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
    public BaseResponse completeAttendance(Long waypointId) {
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
        return BaseResponse.builder()
                .message("해당 경유지의 출석을 완료하였습니다")
                .build();
    }

    @Transactional
    public BaseResponse markPreAbsent(Long studentId, LocalDate absenceDate) {
        // 현재 사용자 정보(인증 객체)로 학부모 여부 확인
        Object userByContextHolder = userDetailsService.getUserByContextHolder();

        if (!(userByContextHolder instanceof Parent)) {
            throw new ParentNotFoundException("해당 기능은 학부모만 사용할 수 있습니다.");
        }

        Parent parent = (Parent) userByContextHolder;

        // 학부모가 신청하는 자녀가 맞는지 검증
        Student findStudent = studentRepository.findById(studentId)
                .filter(student -> student.getParent().getId().equals(parent.getId())) // 학부모의 자녀인지 확인
                .orElseThrow(() -> new StudentNotFoundException("학생 정보를 찾을 수 없거나 자녀가 아닙니다."));

        // 신청한 날짜에 해당하는 출석 정보 조회, 없으면 새로 생성
        Attendance attendance = attendanceRepository.findByStudentAndAttendanceDate(findStudent, absenceDate)
                .orElseGet(() -> Attendance.builder()
                        .student(findStudent)
                        .waypoint(findStudent.getWaypoint())
                        .attendanceDate(absenceDate)  // 신청한 날짜로 설정
                        .attendanceStatus(AttendanceStatus.UNCONFIRMED) // 기본 상태 설정
                        .build());

        // 출석 상태를 "사전 결석"으로 업데이트
        Attendance updatedAttendance = attendance.toBuilder()
                .attendanceStatus(AttendanceStatus.PREABSENT)
                .build();

        attendanceRepository.save(updatedAttendance);
        return BaseResponse.builder()
                .message("사전 결석 신청이 완료되었습니다")
                .build();
    }
}

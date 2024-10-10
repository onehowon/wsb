package com.ebiz.wsb.domain.waypoint.application;

import com.ebiz.wsb.domain.attendance.entity.Attendance;
import com.ebiz.wsb.domain.attendance.entity.AttendanceStatus;
import com.ebiz.wsb.domain.attendance.repository.AttendanceRepository;
import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.student.dto.StudentDTO;
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.student.exception.StudentNotFoundException;
import com.ebiz.wsb.domain.waypoint.dto.WaypointDTO;
import com.ebiz.wsb.domain.waypoint.entity.Waypoint;
import com.ebiz.wsb.domain.waypoint.exception.WaypointWithoutStudentsException;
import com.ebiz.wsb.domain.waypoint.repository.WaypointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WaypointService {

    private final WaypointRepository waypointRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final AttendanceRepository attendanceRepository;

    public List<WaypointDTO> getWaypoints() {
        Object userByContextHolder = userDetailsService.getUserByContextHolder();
        if (userByContextHolder instanceof Guardian) {
            Guardian guardian = (Guardian) userByContextHolder;
            Group group = guardian.getGroup();
            List<Waypoint> waypoints = waypointRepository.findByGroup_Id(group.getId());

            return waypoints.stream()
                    .map(this::convertToDTOWithStudentCount)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private WaypointDTO convertToDTOWithStudentCount(Waypoint waypoint) {
        // Waypoint -> WaypointDTO 변환, 학생 수 포함
        return new WaypointDTO(
                waypoint.getId(),
                waypoint.getWaypointName(),
                waypoint.getLatitude(),
                waypoint.getLongitude(),
                waypoint.getWaypointOrder(),
                waypoint.getGroup().getId(),
                waypoint.getStudents().size()
        );
    }


    public List<StudentDTO> getStudentByWaypoint(Long waypointId) {
        Object userByContextHolder = userDetailsService.getUserByContextHolder();

        if (userByContextHolder instanceof Guardian) {
            Guardian guardian = (Guardian) userByContextHolder;
            Group group = guardian.getGroup();
            List<Waypoint> waypoints = waypointRepository.findByGroup_Id(group.getId());

            for (Waypoint waypoint : waypoints) {
                if (waypoint.getId().equals(waypointId)) {
                    List<Student> students = waypointRepository.findStudentsByWaypointId(waypointId);
                    return students.stream()
                            .map(this::convertToStudentDTO)
                            .collect(Collectors.toList());
                }
            }
        }

        throw new WaypointWithoutStudentsException("해당 경유지에 배정된 학생을 찾을 수가 없습니다.");
    }

    private StudentDTO convertToStudentDTO(Student student) {
        // 오늘 날짜의 출석 상태를 가져옴
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByStudentAndAttendanceDate(student, today)
                .orElse(Attendance.builder()
                        .attendanceStatus(AttendanceStatus.UNCONFIRMED)
                        .build());

        return StudentDTO.builder()
                .studentId(student.getStudentId())
                .name(student.getName())
                .schoolName(student.getSchoolName())
                .grade(student.getGrade())
                .notes(student.getNotes())
                .imagePath(student.getImagePath())
                .attendanceStatus(attendance.getAttendanceStatus())
                .groupId(student.getGroup().getId())
                .waypointId(student.getWaypoint().getId())
                .build();
    }
}

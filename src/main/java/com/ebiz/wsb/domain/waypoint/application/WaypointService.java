package com.ebiz.wsb.domain.waypoint.application;

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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WaypointService {

    private final WaypointRepository waypointRepository;
    private final UserDetailsServiceImpl userDetailsService;

    public List<WaypointDTO> getWaypoints() {
        Object userByContextHolder = userDetailsService.getUserByContextHolder();
        if (userByContextHolder instanceof Guardian) {
            Guardian guardian = (Guardian) userByContextHolder;
            Group group = guardian.getGroup();
            List<Waypoint> waypoints = waypointRepository.findByGroup_Id(group.getId());

            return waypoints.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private WaypointDTO convertToDTO(Waypoint waypoint) {
        return WaypointDTO.builder()
                .waypointId(waypoint.getId())
                .waypointName(waypoint.getWaypointName())
                .latitude(waypoint.getLatitude())
                .longitude(waypoint.getLongitude())
                .waypointOrder(waypoint.getWaypointOrder())
                .groupId(waypoint.getGroup().getId())
                .build();
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
        return StudentDTO.builder()
                .studentId(student.getStudentId())
                .name(student.getName())
                .schoolName(student.getSchoolName())
                .grade(student.getGrade())
                .notes(student.getNotes())
                .imagePath(student.getImagePath())
                .groupId(student.getGroup().getId())
                .build();
    }
}

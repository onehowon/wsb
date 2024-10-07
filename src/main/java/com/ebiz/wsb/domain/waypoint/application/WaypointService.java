package com.ebiz.wsb.domain.waypoint.application;

import com.ebiz.wsb.domain.student.dto.StudentDTO;
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.waypoint.repository.WaypointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WaypointService {

    private final WaypointRepository waypointRepository;

    public List<StudentDTO> getStudentByWaypoint(Long waypointId) {
        List<Student> students = waypointRepository.findStudentsByWaypointId(waypointId);
        return students.stream()
                .map(this::convertToStudentDTO)
                .collect(Collectors.toList());
    }

    private StudentDTO convertToStudentDTO(Student student) {
        return StudentDTO.builder()
                .studentId(student.getStudentId())
                .name(student.getName())
                .schoolName(student.getSchoolName())
                .grade(student.getGrade())
                .notes(student.getNotes())
                .imagePath(student.getImagePath())
                .guardianId(student.getGuardian().getId())
                .routeId(student.getRoute().getId())
                .build();
    }
}

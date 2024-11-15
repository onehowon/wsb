package com.ebiz.wsb.domain.parent.dto;

import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.student.dto.StudentDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ParentMapper {

    public ParentDTO toDTO(Parent parent) {
        return ParentDTO.builder()
                .id(parent.getId())
                .name(parent.getName())
                .phone(parent.getPhone())
                .address(parent.getAddress())
                .imagePath(parent.getImagePath())
                .build();
    }

    public ParentDTO toDTOWithStudents(Parent parent) {
        List<StudentDTO> students = parent.getStudents().stream()
                .map(student -> StudentDTO.builder()
                        .studentId(student.getStudentId())
                        .name(student.getName())
                        .schoolName(student.getSchoolName())
                        .grade(student.getGrade())
                        .notes(student.getNotes())
                        .imagePath(student.getImagePath())
                        .build())
                .collect(Collectors.toList());

        return ParentDTO.builder()
                .id(parent.getId())
                .name(parent.getName())
                .phone(parent.getPhone())
                .address(parent.getAddress())
                .imagePath(parent.getImagePath())
                .students(students)
                .build();
    }
}

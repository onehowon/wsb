package com.ebiz.wsb.domain.student.service;

import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.repository.GroupRepository;
import com.ebiz.wsb.domain.guardian.exception.GuardianNotFoundException;
import com.ebiz.wsb.domain.student.dto.GroupStudentAssignRequest;
import com.ebiz.wsb.domain.student.entity.GroupStudent;
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.student.exception.StudentNotFoundException;
import com.ebiz.wsb.domain.student.repository.GroupStudentRepository;
import com.ebiz.wsb.domain.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupStudentService {

    private final GroupStudentRepository groupStudentRepository;
    private final GroupRepository groupRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public void assignStudentToGroup(GroupStudentAssignRequest request){
        Long studentId = request.getStudentId();
        Long groupId = request.getGroupId();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("학생을 찾을 수 없습니다."));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GuardianNotFoundException("그룹을 찾을 수 없습니다."));

        GroupStudent groupStudent = GroupStudent.builder()
                .student(student)
                .group(group)
                .build();

        groupStudentRepository.save(groupStudent);
    }
}

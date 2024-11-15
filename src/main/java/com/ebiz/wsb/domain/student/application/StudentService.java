package com.ebiz.wsb.domain.student.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.exception.GroupNotFoundException;
import com.ebiz.wsb.domain.group.repository.GroupRepository;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.parent.exception.ParentNotFoundException;
import com.ebiz.wsb.domain.parent.repository.ParentRepository;
import com.ebiz.wsb.domain.student.dto.GroupStudentAssignRequest;
import com.ebiz.wsb.domain.student.dto.StudentCreateRequestDTO;
import com.ebiz.wsb.domain.student.dto.StudentDTO;
import com.ebiz.wsb.domain.student.dto.StudentMapper;
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.student.exception.ImageUploadException;
import com.ebiz.wsb.domain.student.exception.StudentNotAccessException;
import com.ebiz.wsb.domain.student.exception.StudentNotFoundException;
import com.ebiz.wsb.domain.student.repository.StudentRepository;
import com.ebiz.wsb.domain.waypoint.entity.Waypoint;
import com.ebiz.wsb.domain.waypoint.repository.WaypointRepository;
import com.ebiz.wsb.global.service.AuthorizationHelper;
import com.ebiz.wsb.global.service.ImageService;
import com.ebiz.wsb.global.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final GroupRepository groupRepository;
    private final WaypointRepository waypointRepository;
    private final ImageService imageService;
    private final AuthorizationHelper authorizationHelper;
    private final StudentMapper studentMapper;

    @Transactional
    public StudentDTO createStudent(StudentCreateRequestDTO requestDTO, MultipartFile imageFile) {
        Parent parent = authorizationHelper.getLoggedInParent();

        String imagePath = imageService.uploadImage(imageFile, "walkingschoolbus-bucket");

        Student student = Student.builder()
                .name(requestDTO.getName())
                .schoolName(requestDTO.getSchoolName())
                .grade(requestDTO.getGrade())
                .notes(requestDTO.getNotes())
                .imagePath(imagePath)
                .parent(parent)
                .ParentPhone(requestDTO.getParentPhone())
                .build();

        return studentMapper.toDTO(studentRepository.save(student), true);
    }


    public List<StudentDTO> getAllStudents(String userType) {
        Object currentUser = authorizationHelper.getCurrentUser(userType); // PARENT 또는 GUARDIAN 전달

        List<Student> students;
        if (currentUser instanceof Parent parent) {
            students = studentRepository.findAllByParentId(parent.getId());
        } else if (currentUser instanceof Guardian guardian) {
            Group group = guardian.getGroup();
            if (group == null) {
                throw new GroupNotFoundException("해당 지도사는 그룹에 속해 있지 않습니다.");
            }
            students = studentRepository.findAllByGroupId(group.getId());
        } else {
            throw new StudentNotAccessException("학생 정보를 조회할 권한이 없습니다.");
        }

        return students.stream().map(student -> studentMapper.toDTO(student, true)).toList();
    }



    @Transactional
    public StudentDTO getStudentById(Long studentId, String userType) {
        Student student = findStudentById(studentId);

        Object currentUser = authorizationHelper.getCurrentUser(userType); // PARENT 또는 GUARDIAN 전달
        if (currentUser instanceof Parent parent) {
            authorizationHelper.validateParentAccess(student.getParent(), parent.getId());
        } else if (currentUser instanceof Guardian guardian) {
            Group group = student.getGroup();
            if (group == null || !group.getId().equals(guardian.getGroup().getId())) {
                throw new StudentNotAccessException("해당 그룹의 학생 정보를 조회할 권한이 없습니다.");
            }
        } else {
            throw new StudentNotAccessException("학생 정보를 조회할 권한이 없습니다.");
        }

        return studentMapper.toDTO(student, true);
    }

    @Transactional
    public StudentDTO updateStudent(Long studentId, StudentCreateRequestDTO requestDTO, MultipartFile imageFile) {
        Student existingStudent = findStudentById(studentId);
        Parent parent = authorizationHelper.getLoggedInParent();
        authorizationHelper.validateParentAccess(existingStudent.getParent(), parent.getId());

        String imagePath = imageService.uploadImage(imageFile, "walkingschoolbus-bucket");
        if (imagePath == null) {
            imagePath = existingStudent.getImagePath();
        }

        Student updatedStudent = Student.builder()
                .studentId(existingStudent.getStudentId())
                .name(requestDTO.getName())
                .schoolName(requestDTO.getSchoolName())
                .grade(requestDTO.getGrade())
                .notes(requestDTO.getNotes())
                .imagePath(imagePath)
                .parent(existingStudent.getParent())
                .group(existingStudent.getGroup())
                .waypoint(existingStudent.getWaypoint())
                .ParentPhone(requestDTO.getParentPhone())
                .build();

        return studentMapper.toDTO(studentRepository.save(updatedStudent), false);
    }

    @Transactional
    public StudentDTO assignGroupAndWaypoint(Long studentId, GroupStudentAssignRequest request) {
        Student existingStudent = findStudentById(studentId);

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException("그룹을 찾을 수 없습니다."));
        Waypoint waypoint = waypointRepository.findById(request.getWaypointId())
                .orElseThrow(() -> new RuntimeException("경유지를 찾을 수 없습니다."));

        Student updatedStudent = Student.builder()
                .studentId(existingStudent.getStudentId())
                .name(existingStudent.getName())
                .schoolName(existingStudent.getSchoolName())
                .grade(existingStudent.getGrade())
                .notes(existingStudent.getNotes())
                .imagePath(existingStudent.getImagePath())
                .parent(existingStudent.getParent())
                .group(group)
                .waypoint(waypoint)
                .ParentPhone(existingStudent.getParentPhone())
                .build();

        return studentMapper.toDTO(studentRepository.save(updatedStudent), true);
    }



    @Transactional
    public void deleteStudent(Long studentId) {
        Student student = findStudentById(studentId);
        Parent parent = authorizationHelper.getLoggedInParent();
        authorizationHelper.validateParentAccess(student.getParent(), parent.getId());

        if (student.getImagePath() != null) {
            imageService.deleteImage(student.getImagePath(), "walkingschoolbus-bucket");
        }

        studentRepository.delete(student);
    }

    private Student findStudentById(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("학생을 찾을 수 없습니다."));
    }
}

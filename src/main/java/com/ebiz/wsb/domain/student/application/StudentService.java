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
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.student.exception.ImageUploadException;
import com.ebiz.wsb.domain.student.exception.StudentNotAccessException;
import com.ebiz.wsb.domain.student.exception.StudentNotFoundException;
import com.ebiz.wsb.domain.student.repository.StudentRepository;
import com.ebiz.wsb.domain.waypoint.entity.Waypoint;
import com.ebiz.wsb.domain.waypoint.repository.WaypointRepository;
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
    private final S3Service s3Service;
    private final GroupRepository groupRepository;
    private final WaypointRepository waypointRepository;
    private final ParentRepository parentRepository;
    private final UserDetailsServiceImpl userDetailsService;

    @Transactional
    public StudentDTO createStudent(StudentCreateRequestDTO studentCreateRequestDTO, MultipartFile imageFile) {

        Long parentId = getLoggedInParentId();

        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ParentNotFoundException("부모 정보를 찾을 수 없습니다."));

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                imageUrl = s3Service.uploadImageFile(imageFile, "walkingschoolbus-bucket");
            } catch (IOException e) {
                throw new ImageUploadException("이미지 업로드 중 오류가 발생했습니다.", e);
            }
        }

        Student student = Student.builder()
                .name(studentCreateRequestDTO.getName())
                .schoolName(studentCreateRequestDTO.getSchoolName())
                .grade(studentCreateRequestDTO.getGrade())
                .notes(studentCreateRequestDTO.getNotes())
                .imagePath(imageUrl)
                .parent(parent)
                .build();

        student = studentRepository.save(student);

        return convertToDTOWithoutGroupAndWaypoint(student);
    }


    public List<StudentDTO> getAllStudents() {
        Object currentUser = userDetailsService.getUserByContextHolder();

        List<Student> students;

        if (currentUser instanceof Parent) {
            Parent currentParent = (Parent) currentUser;
            students = studentRepository.findAllByParentId(currentParent.getId());

        } else if (currentUser instanceof Guardian) {
            Guardian currentGuardian = (Guardian) currentUser;

            if (currentGuardian.getGroup() == null) {
                throw new GroupNotFoundException("해당 지도사는 그룹에 속해 있지 않습니다.");
            }

            students = studentRepository.findAllByGroupId(currentGuardian.getGroup().getId());

        } else {
            throw new StudentNotAccessException("학생 정보를 조회할 권한이 없습니다.");
        }

        return students.stream().map(this::convertToDTOWithGroupAndWaypoint).toList();
    }



    @Transactional
    public StudentDTO getStudentById(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("학생을 찾을 수 없습니다."));

        Object currentUser = userDetailsService.getUserByContextHolder();

        if (currentUser instanceof Parent) {
            Parent currentParent = (Parent) currentUser;

            if (!student.getParent().getId().equals(currentParent.getId())) {
                throw new StudentNotAccessException("본인의 자녀 정보만 조회할 수 있습니다.");
            }

            return convertToDTOWithGroupAndWaypoint(student);
        }

        if (currentUser instanceof Guardian) {
            Guardian currentGuardian = (Guardian) currentUser;

            if (student.getGroup() == null || !student.getGroup().getId().equals(currentGuardian.getGroup().getId())) {
                throw new StudentNotAccessException("해당 그룹의 학생 정보를 조회할 권한이 없습니다.");
            }

            return convertToDTOWithGroupAndWaypoint(student);
        }
        throw new StudentNotAccessException("해당 학생 정보를 조회할 권한이 없습니다.");
    }

    @Transactional
    public StudentDTO updateStudent(Long studentId, StudentCreateRequestDTO studentCreateRequestDTO, MultipartFile imageFile) {
        Student existingStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("학생 정보를 찾을 수 없습니다."));

        Long parentId = getLoggedInParentId();
        if (!existingStudent.getParent().getId().equals(parentId)) {
            throw new StudentNotAccessException("해당 학생을 수정할 권한이 없습니다.");
        }

        String imageUrl = existingStudent.getImagePath();
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImage(imageFile);
        }

        existingStudent = Student.builder()
                .studentId(existingStudent.getStudentId())
                .name(studentCreateRequestDTO.getName())
                .schoolName(studentCreateRequestDTO.getSchoolName())
                .grade(studentCreateRequestDTO.getGrade())
                .notes(studentCreateRequestDTO.getNotes())
                .imagePath(imageUrl)
                .parent(existingStudent.getParent())
                .build();

        studentRepository.save(existingStudent);

        return convertToDTOWithoutGroupAndWaypoint(existingStudent);
    }

    @Transactional
    public StudentDTO assignGroupAndWaypoint(Long studentId, GroupStudentAssignRequest request) {

        Student existingStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("학생을 찾을 수 없습니다."));

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new RuntimeException("그룹을 찾을 수 없습니다."));

        Waypoint waypoint = waypointRepository.findById(request.getWaypointId())
                .orElseThrow(() -> new RuntimeException("경유지를 찾을 수 없습니다."));

        Student updatedStudent = Student.builder()
                .studentId(existingStudent.getStudentId())
                .name(existingStudent.getName())
                .schoolName(existingStudent.getSchoolName())
                .grade(existingStudent.getGrade())
                .notes(existingStudent.getNotes())
                .imagePath(existingStudent.getImagePath())
                .group(group)
                .waypoint(waypoint)
                .ParentPhone(existingStudent.getParentPhone())
                .build();

        studentRepository.save(updatedStudent);
        return convertToDTOWithGroupAndWaypoint(updatedStudent);
    }



    @Transactional
    public void deleteStudent(Long studentId) {
        Student existingStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("학생을 찾을 수 없습니다."));

        Long parentId = getLoggedInParentId();
        if (!existingStudent.getParent().getId().equals(parentId)) {
            throw new IllegalArgumentException("해당 학생을 삭제할 권한이 없습니다.");
        }

        studentRepository.deleteById(studentId);
    }

    private StudentDTO convertToDTOWithoutGroupAndWaypoint(Student student) {
        return StudentDTO.builder()
                .studentId(student.getStudentId())
                .name(student.getName())
                .schoolName(student.getSchoolName())
                .grade(student.getGrade())
                .notes(student.getNotes())
                .imagePath(student.getImagePath())
                .parentId(student.getParent().getId())
                .parentPhone(student.getParent().getPhone())
                .build();
    }

    private StudentDTO convertToDTOWithGroupAndWaypoint(Student student) {
        return StudentDTO.builder()
                .studentId(student.getStudentId())
                .name(student.getName())
                .schoolName(student.getSchoolName())
                .grade(student.getGrade())
                .notes(student.getNotes())
                .imagePath(student.getImagePath())
                .groupId(student.getGroup() != null ? student.getGroup().getId() : null)
                .groupName(student.getGroup() != null ? student.getGroup().getGroupName() : null)
                .waypointId(student.getWaypoint() != null ? student.getWaypoint().getId() : null)
                .waypointName(student.getWaypoint() != null ? student.getWaypoint().getWaypointName() : null)
                .parentId(student.getParent().getId())
                .parentPhone(student.getParent().getPhone())
                .build();
    }

    public Long getLoggedInParentId() {
        Object currentUser = userDetailsService.getUserByContextHolder();

        if (currentUser instanceof Parent) {
            Parent parent = (Parent) currentUser;
            return parent.getId();
        } else {
            throw new StudentNotAccessException("부모만 학생 정보를 수정할 수 있습니다.");
        }
    }


    private String uploadImage(MultipartFile imageFile) {
        try {
            String imageUrl = s3Service.uploadImageFile(imageFile, "walkingschoolbus-bucket");
            return imageUrl;
        } catch (IOException e) {
            throw new ImageUploadException("이미지 업로드 실패", e);
        }
    }
}

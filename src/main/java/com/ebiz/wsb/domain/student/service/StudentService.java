package com.ebiz.wsb.domain.student.service;

import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.exception.GroupNotFoundException;
import com.ebiz.wsb.domain.group.repository.GroupRepository;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.exception.GuardianNotFoundException;
import com.ebiz.wsb.domain.guardian.repository.GuardianRepository;
import com.ebiz.wsb.domain.student.dto.StudentCreateRequestDTO;
import com.ebiz.wsb.domain.student.dto.StudentDTO;
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.student.exception.ImageUploadException;
import com.ebiz.wsb.domain.student.exception.StudentNotFoundException;
import com.ebiz.wsb.domain.student.repository.StudentRepository;
import com.ebiz.wsb.domain.waypoint.entity.Waypoint;
import com.ebiz.wsb.domain.waypoint.exception.WaypointNotFoundException;
import com.ebiz.wsb.domain.waypoint.repository.WaypointRepository;
import com.ebiz.wsb.global.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final GuardianRepository guardianRepository;
    private final GroupRepository groupRepository;
    private final S3Service s3Service;
    private final WaypointRepository waypointRepository;

    @Transactional
    public StudentDTO createStudent(StudentCreateRequestDTO studentCreateRequestDTO, MultipartFile imageFile) {
        validateStudentDTO(studentCreateRequestDTO);

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                imageUrl = s3Service.uploadImageFile(imageFile, "walkingschoolbus-bucket");
            } catch (IOException e) {
                throw new ImageUploadException("이미지 업로드 중 오류가 발생했습니다.", e);
            }
        }

        Group group = groupRepository.findById(studentCreateRequestDTO.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException("그룹 정보를 찾을 수 없습니다."));
        Waypoint waypoint = waypointRepository.findById(studentCreateRequestDTO.getWaypointId())
                .orElseThrow(() -> new WaypointNotFoundException("해당 경유지를 찾을 수 없습니다."));

        Student student = Student.builder()
                .name(studentCreateRequestDTO.getName())
                .group(group)
                .waypoint(waypoint)
                .schoolName(studentCreateRequestDTO.getSchoolName())
                .grade(studentCreateRequestDTO.getGrade())
                .notes(studentCreateRequestDTO.getNotes())
                .imagePath(imageUrl)
                .build();

        student = studentRepository.save(student);

        return convertToDTO(student);
    }


    public List<StudentDTO> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        return students.stream().map(this::convertToDTO).toList();
    }

    public StudentDTO getStudentById(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("학생 정보를 찾을 수 없습니다."));
        return convertToDTO(student);
    }

    @Transactional
    public StudentDTO updateStudent(Long studentId, StudentCreateRequestDTO studentCreateRequestDTO, MultipartFile imageFile) {
        Student existingStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("학생 정보를 찾을 수 없습니다."));

        String imageUrl = existingStudent.getImagePath();
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImage(imageFile);
        }

        Group group = groupRepository.findById(studentCreateRequestDTO.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException("그룹 정보를 찾을 수 없습니다."));
        Waypoint waypoint = waypointRepository.findById(studentCreateRequestDTO.getWaypointId())
                .orElseThrow(() -> new WaypointNotFoundException("해당 경유지를 찾을 수 없습니다."));

        existingStudent = Student.builder()
                .studentId(existingStudent.getStudentId())
                .name(studentCreateRequestDTO.getName())
                .group(group)
                .waypoint(waypoint)
                .schoolName(studentCreateRequestDTO.getSchoolName())
                .grade(studentCreateRequestDTO.getGrade())
                .notes(studentCreateRequestDTO.getNotes())
                .imagePath(imageUrl)
                .build();

        studentRepository.save(existingStudent);

        return convertToDTO(existingStudent);
    }

    @Transactional
    public void deleteStudent(Long studentId) {
        studentRepository.deleteById(studentId);
    }

    private StudentDTO convertToDTO(Student student) {
        return StudentDTO.builder()
                .studentId(student.getStudentId())
                .name(student.getName())
                .groupId(student.getGroup().getId())
                .waypointId(student.getWaypoint().getId())
                .schoolName(student.getSchoolName())
                .grade(student.getGrade())
                .notes(student.getNotes())
                .imagePath(student.getImagePath())
                .build();
    }

    private void validateStudentDTO(StudentCreateRequestDTO studentDTO) {
        if (studentDTO.getName() == null || studentDTO.getName().isEmpty()) {
            throw new IllegalArgumentException("학생 이름은 필수 항목입니다.");
        }
        if (studentDTO.getGroupId() == null) {
            throw new IllegalArgumentException("Group ID는 필수 항목입니다.");
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

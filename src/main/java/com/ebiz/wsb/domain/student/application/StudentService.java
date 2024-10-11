package com.ebiz.wsb.domain.student.application;

import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.repository.GroupRepository;
import com.ebiz.wsb.domain.student.dto.GroupStudentAssignRequest;
import com.ebiz.wsb.domain.student.dto.StudentCreateRequestDTO;
import com.ebiz.wsb.domain.student.dto.StudentDTO;
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.student.exception.ImageUploadException;
import com.ebiz.wsb.domain.student.exception.StudentNotFoundException;
import com.ebiz.wsb.domain.student.repository.StudentRepository;
import com.ebiz.wsb.domain.waypoint.entity.Waypoint;
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
    private final S3Service s3Service;
    private final GroupRepository groupRepository;
    private final WaypointRepository waypointRepository;

    @Transactional
    public StudentDTO createStudent(StudentCreateRequestDTO studentCreateRequestDTO, MultipartFile imageFile) {

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
                .ParentPhone(studentCreateRequestDTO.getParentPhone())
                .build();

        student = studentRepository.save(student);

        return convertToDTOWithoutGroupAndWaypoint(student);
    }


    public List<StudentDTO> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        return students.stream().map(this::convertToDTOWithGroupAndWaypoint).toList();
    }


    public StudentDTO getStudentById(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("학생을 찾을 수 없습니다."));

        return convertToDTOWithGroupAndWaypoint(student);
    }

    @Transactional
    public StudentDTO updateStudent(Long studentId, StudentCreateRequestDTO studentCreateRequestDTO, MultipartFile imageFile) {
        Student existingStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("학생 정보를 찾을 수 없습니다."));

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
                .ParentPhone(studentCreateRequestDTO.getParentPhone())
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
                .ParentPhone(student.getParentPhone())
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
                .waypointId(student.getWaypoint() != null ? student.getWaypoint().getId() : null)
                .waypointName(student.getWaypoint() != null ? student.getWaypoint().getWaypointName() : null)
                .ParentPhone(student.getParentPhone())
                .build();
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

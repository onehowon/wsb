package com.ebiz.wsb.domain.student.service;

import com.ebiz.wsb.domain.route.entity.Route;
import com.ebiz.wsb.domain.route.exception.RouteNotFoundException;
import com.ebiz.wsb.domain.route.repository.RouteRepository;
import com.ebiz.wsb.domain.student.dto.StudentDTO;
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.student.exception.ImageUploadException;
import com.ebiz.wsb.domain.student.exception.StudentNotFoundException;
import com.ebiz.wsb.domain.student.repository.StudentRepository;
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
    private final RouteRepository routeRepository;
    private final S3Service s3Service;

    @Transactional
    public StudentDTO createStudent(StudentDTO studentDTO, MultipartFile imageFile) {
        validateStudentDTO(studentDTO);

        String imageUrl = null;

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                imageUrl = s3Service.uploadImageFile(imageFile, "walkingschoolbus-bucket");
            } catch (IOException e) {
                throw new ImageUploadException("이미지 업로드 중 오류가 발생했습니다.", e);
            }
        }

        Route route = routeRepository.findById(studentDTO.getRouteId())
                .orElseThrow(() -> new RouteNotFoundException("해당 경로를 찾을 수 없습니다."));

        Student student = Student.builder()
                .name(studentDTO.getName())
                .guardianContact(studentDTO.getGuardianContact())
                .route(route)
                .schoolName(studentDTO.getSchoolName())
                .grade(studentDTO.getGrade())
                .notes(studentDTO.getNotes())
                .imagePath(imageUrl)
                .build();

        student = studentRepository.save(student);

        return StudentDTO.builder()
                .studentId(student.getStudentId())
                .name(student.getName())
                .guardianContact(student.getGuardianContact())
                .routeId(student.getRoute().getRouteId())
                .schoolName(student.getSchoolName())
                .grade(student.getGrade())
                .notes(student.getNotes())
                .imagePath(student.getImagePath())
                .build();
    }

    public List<StudentDTO> getAllStudents(){
        List<Student> students = studentRepository.findAll();
        return students.stream().map(this::convertToDTO).toList();
    }

    public StudentDTO getStudentById(Long studentId){
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("학생을 찾을 수 없습니다."));
        return StudentDTO.builder()
                .studentId(student.getStudentId())
                .name(student.getName())
                .guardianContact(student.getGuardianContact())
                .routeId(student.getRoute().getRouteId())
                .schoolName(student.getSchoolName())
                .grade(student.getGrade())
                .notes(student.getNotes())
                .imagePath(student.getImagePath())
                .build();
    }

    @Transactional
    public StudentDTO updateStudent(Long studentId, StudentDTO studentDTO, MultipartFile imageFile) {
        Student existingStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("학생 정보를 찾을 수 없습니다."));

        // 이미지 업로드 처리
        String imageUrl = existingStudent.getImagePath();
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImage(imageFile);
        }

        Route route = routeRepository.findById(studentDTO.getRouteId())
                .orElseThrow(() -> new RouteNotFoundException("경로를 찾을 수 없습니다."));

        existingStudent = Student.builder()
                .studentId(existingStudent.getStudentId())  // 기존 studentId 유지
                .name(studentDTO.getName())
                .guardianContact(studentDTO.getGuardianContact())
                .route(routeRepository.findById(studentDTO.getRouteId())
                        .orElseThrow(() -> new RouteNotFoundException("경로를 찾을 수 없습니다.")))
                .schoolName(studentDTO.getSchoolName())
                .grade(studentDTO.getGrade())
                .notes(studentDTO.getNotes())
                .imagePath(imageUrl)  // 업데이트된 이미지 경로
                .build();

        studentRepository.save(existingStudent);

        return StudentDTO.builder()
                .studentId(existingStudent.getStudentId())  // studentId 반환
                .name(existingStudent.getName())
                .guardianContact(existingStudent.getGuardianContact())
                .routeId(existingStudent.getRoute().getRouteId())
                .schoolName(existingStudent.getSchoolName())
                .grade(existingStudent.getGrade())
                .notes(existingStudent.getNotes())
                .imagePath(existingStudent.getImagePath())  // 업데이트된 imagePath 반환
                .build();
    }

    @Transactional
    public void deleteStudent(Long studentId){
        studentRepository.deleteById(studentId);
    }

    private StudentDTO convertToDTO(Student student) {
        return StudentDTO.builder()
                .name(student.getName())
                .guardianContact(student.getGuardianContact())
                .routeId(student.getRoute() != null ? student.getRoute().getRouteId() : null)
                .schoolName(student.getSchoolName())
                .grade(student.getGrade())
                .notes(student.getNotes())
                .build();
    }

    private void validateStudentDTO(StudentDTO studentDTO) {
        System.out.println("Received studentDTO: " + studentDTO); // 디버깅용 로그
        if (studentDTO.getName() == null || studentDTO.getName().isEmpty()) {
            throw new IllegalArgumentException("학생 이름은 필수 항목입니다.");
        }
        if (studentDTO.getRouteId() == null) {
            throw new IllegalArgumentException("Route ID는 필수 항목입니다.");
        }
    }

    private String uploadImage(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        try {
            // S3에 이미지 업로드
            return s3Service.uploadImageFile(imageFile, "walkingschoolbus-bucket");
        } catch (IOException e) {
            throw new ImageUploadException("이미지 업로드 실패", e);
        }
    }
}

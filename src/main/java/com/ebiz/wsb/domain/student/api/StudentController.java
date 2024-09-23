package com.ebiz.wsb.domain.student.api;

import com.ebiz.wsb.domain.student.dto.StudentCreateRequestDTO;
import com.ebiz.wsb.domain.student.dto.StudentDTO;
import com.ebiz.wsb.domain.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentDTO> createStudent(
            @RequestParam("name") String name,
            @RequestParam("guardianId") Long guardianId,
            @RequestParam("routeId") Long routeId,
            @RequestParam("schoolName") String schoolName,
            @RequestParam("grade") String grade,
            @RequestParam("notes") String notes,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        StudentCreateRequestDTO studentCreateRequestDTO = StudentCreateRequestDTO.builder()
                .name(name)
                .guardianId(guardianId)
                .routeId(routeId)
                .schoolName(schoolName)
                .grade(grade)
                .notes(notes)
                .build();

        StudentDTO createdStudent = studentService.createStudent(studentCreateRequestDTO, imageFile);
        return ResponseEntity.ok(createdStudent);
    }

    @GetMapping
    public ResponseEntity<List<StudentDTO>> getAllStudents(){
        List<StudentDTO> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{studentId}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long studentId){
        StudentDTO studentDTO = studentService.getStudentById(studentId);
        return ResponseEntity.ok(studentDTO);
    }

    @PutMapping("/{studentId}")
    public ResponseEntity<StudentDTO> updateStudent(
            @PathVariable Long studentId,
            @RequestParam("name") String name,
            @RequestParam("guardianId") Long guardianId,
            @RequestParam("routeId") Long routeId,
            @RequestParam("schoolName") String schoolName,
            @RequestParam("grade") String grade,
            @RequestParam("notes") String notes,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        StudentCreateRequestDTO studentCreateRequestDTO = StudentCreateRequestDTO.builder()
                .name(name)
                .guardianId(guardianId)
                .routeId(routeId)
                .schoolName(schoolName)
                .grade(grade)
                .notes(notes)
                .build();

        StudentDTO updatedStudent = studentService.updateStudent(studentId, studentCreateRequestDTO, imageFile);
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long studentId){
        studentService.deleteStudent(studentId);
        return ResponseEntity.noContent().build();
    }
}

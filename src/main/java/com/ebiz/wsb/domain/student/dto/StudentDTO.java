package com.ebiz.wsb.domain.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentDTO {
    private Long studentId;
    private String name;
    private String guardianContact;
    private Long routeId;
    private String schoolName;
    private String grade;
    private String notes;
    private String imagePath;

}

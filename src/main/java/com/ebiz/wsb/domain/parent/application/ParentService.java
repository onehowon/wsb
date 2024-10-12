package com.ebiz.wsb.domain.parent.application;

import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.parent.dto.ParentDTO;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.parent.exception.ParentNotFoundException;
import com.ebiz.wsb.domain.parent.repository.ParentRepository;
import com.ebiz.wsb.domain.student.dto.StudentDTO;
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.student.exception.ImageUploadException;
import com.ebiz.wsb.global.service.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParentService {

    private static final String PARENT_NOT_FOUND_MESSAGE = "부모 정보를 찾을 수 없습니다.";

    private final ParentRepository parentRepository;
    private final S3Service s3Service;

    public List<ParentDTO> getAllParents() {
        return parentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParentDTO getParentById(Long parentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ParentNotFoundException(PARENT_NOT_FOUND_MESSAGE));

        List<StudentDTO> studentDTOs = parent.getStudents().stream()
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
                .students(studentDTOs)
                .build();
    }

    @Transactional
    public ParentDTO updateParent(Long parentId, ParentDTO parentDTO, MultipartFile imageFile) {
        Parent existingParent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ParentNotFoundException(PARENT_NOT_FOUND_MESSAGE));

        String email = existingParent.getEmail();
        String password = existingParent.getPassword();

        String imageUrl = existingParent.getImagePath();
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImage(imageFile);
        }

        existingParent = Parent.builder()
                .id(existingParent.getId())
                .name(existingParent.getName())
                .phone(parentDTO.getPhone())
                .address(parentDTO.getAddress())
                .email(email)
                .imagePath(imageUrl)
                .password(password)
                .build();

        Parent savedParent = parentRepository.save(existingParent);
        return convertToDTO(savedParent);
    }

    public void deleteParent(Long parentsId) {
        parentRepository.deleteById(parentsId);
    }

    private ParentDTO convertToDTO(Parent parent) {
        return ParentDTO.builder()
                .id(parent.getId())
                .name(parent.getName())
                .phone(parent.getPhone())
                .address(parent.getAddress())
                .imagePath(parent.getImagePath())
                .build();
    }

    private String uploadImage(MultipartFile imageFile) {
        try {
            String imageUrl = s3Service.uploadImageFile(imageFile, "walkingschoolbus-bucket");
            System.out.println("Uploaded image URL: " + imageUrl);  // 이미지 URL 출력
            return imageUrl;
        } catch (IOException e) {
            throw new ImageUploadException("이미지 업로드 실패", e);
        }
    }
}


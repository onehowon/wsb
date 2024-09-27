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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParentService {

    private static final String PARENT_NOT_FOUND_MESSAGE = "부모 정보를 찾을 수 없습니다.";

    private final ParentRepository parentRepository;
    private final S3Service s3Service;

    public List<ParentDTO> getAllParents() {
        List<ParentDTO> parentListDTO = new ArrayList<>();
        List<Parent> parentList = parentRepository.findAll();
        for (Parent parent : parentList) {
            ParentDTO parentDTO = ParentDTO.builder()
                    .id(parent.getId())
                    .name(parent.getName())
                    .email(parent.getEmail())
                    .phone(parent.getPhone())
                    .address(parent.getAddress())
                    .imagePath(parent.getImagePath())
                    .password(parent.getPassword())
                    .build();
            parentListDTO.add(parentDTO);
        }
        return parentListDTO;
    }

    public ParentDTO getParentById(Long parentId) {
        Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ParentNotFoundException(PARENT_NOT_FOUND_MESSAGE));
        ParentDTO parentDTO = ParentDTO.builder()
                .id(parent.getId())
                .name(parent.getName())
                .email(parent.getEmail())
                .phone(parent.getPhone())
                .address(parent.getAddress())
                .imagePath(parent.getImagePath())
                .build();
        return parentDTO;
    }

    @Transactional
    public ParentDTO updateParentImage(Long parentId, MultipartFile imagePath) {
        Parent existingParent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ParentNotFoundException(PARENT_NOT_FOUND_MESSAGE));

        String imageUrl = existingParent.getImagePath();
        if(!imagePath.isEmpty() && imagePath != null) {
            imageUrl = uploadImage(imagePath);
        }

        existingParent = Parent.builder()
                .id(existingParent.getId())
                .name(existingParent.getName())
                .email(existingParent.getEmail())
                .phone(existingParent.getPhone())
                .address(existingParent.getAddress())
                .imagePath(imageUrl)
                .password(existingParent.getPassword())
                .build();

        Parent saveParent = parentRepository.save(existingParent);
        return convertToDTO(saveParent);
    }

    public void deleteParent(Long parentsId) {
        parentRepository.deleteById(parentsId);
    }

    private ParentDTO convertToDTO(Parent parent) {
        return ParentDTO.builder()
                .id(parent.getId())
                .name(parent.getName())
                .email(parent.getEmail())
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

    public ParentDTO updateParentAddress(Long parentId, ParentDTO parentDTO) {
        Parent existingParent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ParentNotFoundException(PARENT_NOT_FOUND_MESSAGE));

        Parent parent = Parent.builder()
                .id(existingParent.getId())
                .name(existingParent.getName())
                .email(existingParent.getEmail())
                .phone(existingParent.getPhone())
                .password(existingParent.getPassword())
                .address(parentDTO.getAddress())
                .imagePath(existingParent.getImagePath())
                .build();

        Parent saveParent = parentRepository.save(parent);
        return convertToDTO(saveParent);
    }
}


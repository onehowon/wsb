package com.ebiz.wsb.domain.parent.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.group.dto.GroupDTO;
import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.exception.GroupNotFoundException;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.exception.GuardianNotFoundException;
import com.ebiz.wsb.domain.parent.dto.ParentDTO;
import com.ebiz.wsb.domain.parent.dto.ParentMapper;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.parent.exception.ParentAccessException;
import com.ebiz.wsb.domain.parent.exception.ParentNotFoundException;
import com.ebiz.wsb.domain.parent.repository.ParentRepository;
import com.ebiz.wsb.domain.student.dto.StudentDTO;
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.student.exception.ImageUploadException;
import com.ebiz.wsb.global.service.AuthorizationHelper;
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
    private final UserDetailsServiceImpl userDetailsService;
    private final AuthorizationHelper authorizationHelper;
    private final ParentMapper parentMapper;

    // 모든 부모를 조회하는 경우는 어떨 때 있는지 ..?
    public List<ParentDTO> getAllParents() {
        return parentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParentDTO getParentById(Long parentId) {
        Object currentUser = userDetailsService.getUserByContextHolder(); // 기본 메서드 사용

        if (currentUser instanceof Parent) {
            Parent loggedInParent = (Parent) currentUser;

            if (!loggedInParent.getId().equals(parentId)) {
                throw new ParentAccessException("본인의 정보만 조회할 수 있습니다.");
            }

            return retrieveParentInfo(parentId);
        } else if (currentUser instanceof Guardian) {
            Guardian guardian = (Guardian) currentUser;

            Parent parentToView = parentRepository.findById(parentId)
                    .orElseThrow(() -> new ParentNotFoundException(PARENT_NOT_FOUND_MESSAGE));

            if (guardian.getGroup() == null || !guardian.getGroup().getId().equals(parentToView.getGroup().getId())) {
                throw new ParentAccessException("해당 그룹의 부모 정보를 조회할 수 없습니다.");
            }

            return retrieveParentInfo(parentId);
        } else {
            throw new ParentAccessException("부모 정보를 조회할 권한이 없습니다.");
        }
    }

    @Transactional
    public ParentDTO updateParent(Long parentId, ParentDTO parentDTO, MultipartFile imageFile) {
        Long loggedInParentId = getLoggedInParentId();

        if (!loggedInParentId.equals(parentId)) {
            throw new IllegalArgumentException("본인의 정보만 수정할 수 있습니다.");
        }

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
                .name(parentDTO.getName())
                .phone(parentDTO.getPhone())
                .address(parentDTO.getAddress())
                .email(email)
                .imagePath(imageUrl)
                .password(password)
                .build();

        Parent savedParent = parentRepository.save(existingParent);
        return convertToDTO(savedParent);
    }

    @Transactional
    public void deleteParent(Long parentId) {
        Long loggedInParentId = getLoggedInParentId();

        if (!loggedInParentId.equals(parentId)) {
            throw new ParentAccessException("본인의 계정만 삭제할 수 있습니다.");
        }

        parentRepository.deleteById(parentId);
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

    public Long getLoggedInParentId() {
        Parent parent = (Parent) userDetailsService.getUserByContextHolder();
        return parent.getId();
    }

    private ParentDTO retrieveParentInfo(Long parentId) {
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

    public GroupDTO getParentGroup() {
        Object userByContextHolder = userDetailsService.getUserByContextHolder();
        if (!(userByContextHolder instanceof Parent)) {
            throw new ParentNotFoundException("학부모 정보를 찾을 수 없습니다.");
        }

        Parent parent = (Parent) userByContextHolder;
        Group group = parent.getGroup();
        if (group == null) {
            throw new GroupNotFoundException("배정된 그룹을 찾을 수 없습니다.");
        }

        return GroupDTO.builder()
                .groupName(group.getGroupName())
                .schoolName(group.getSchoolName())
                .dutyGuardianId(group.getDutyGuardianId())
                .id(group.getId())
                .build();
    }
}


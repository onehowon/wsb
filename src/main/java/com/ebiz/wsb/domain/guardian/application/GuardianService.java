package com.ebiz.wsb.domain.guardian.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.group.dto.GroupDTO;
import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.exception.GroupNotFoundException;
import com.ebiz.wsb.domain.group.repository.GroupRepository;
import com.ebiz.wsb.domain.guardian.dto.GuardianDTO;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.exception.FileUploadException;
import com.ebiz.wsb.domain.guardian.exception.GuardianNotAccessException;
import com.ebiz.wsb.domain.guardian.exception.GuardianNotFoundException;
import com.ebiz.wsb.domain.guardian.repository.GuardianRepository;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.waypoint.entity.Waypoint;
import com.ebiz.wsb.global.service.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuardianService {

    private final GuardianRepository guardianRepository;
    private final S3Service s3service;
    private final UserDetailsServiceImpl userDetailsService;
    private final GroupRepository groupRepository;

    public GuardianDTO getGuardianById(Long guardianId) {
        Object currentUser = userDetailsService.getUserByContextHolder();

        Guardian guardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> new GuardianNotFoundException("지도사 정보를 찾을 수 없습니다."));

        if (currentUser instanceof Guardian) {
            Guardian loggedInGuardian = (Guardian) currentUser;

            if (!loggedInGuardian.getId().equals(guardianId)) {
                throw new GuardianNotAccessException("본인의 정보만 조회할 수 있습니다.");
            }
        } else if (currentUser instanceof Parent) {
            Parent loggedInParent = (Parent) currentUser;

            if (guardian.getGroup() == null || !guardian.getGroup().getId().equals(loggedInParent.getGroup().getId())) {
                throw new GuardianNotAccessException("해당 그룹의 지도사 정보를 조회할 수 없습니다.");
            }
        } else {
            throw new GuardianNotAccessException("해당 지도사 정보를 조회할 권한이 없습니다.");
        }

        return convertToDTO(guardian);
    }

    @Transactional
    public GuardianDTO updateGuardian(Long guardianId, GuardianDTO guardianDTO, MultipartFile imageFile) {
        checkGuardianOwnership(guardianId);

        Guardian existingGuardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> new GuardianNotFoundException("지도사 정보를 찾을 수 없습니다."));

        Group group = existingGuardian.getGroup();

        if (guardianDTO.getGroupId() != null) {
            group = groupRepository.findById(guardianDTO.getGroupId())
                    .orElseThrow(() -> new GroupNotFoundException("해당 그룹을 찾을 수 없습니다."));
        }

        String imageUrl = existingGuardian.getImagePath();
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImage(imageFile);
        }

        Guardian updatedGuardian = Guardian.builder()
                .id(existingGuardian.getId())
                .name(existingGuardian.getName())
                .phone(existingGuardian.getPhone())
                .bio(guardianDTO.getBio() != null ? guardianDTO.getBio() : existingGuardian.getBio())
                .experience(guardianDTO.getExperience() != null ? guardianDTO.getExperience() : existingGuardian.getExperience())
                .imagePath(imageUrl)
                .email(existingGuardian.getEmail())
                .password(existingGuardian.getPassword())
                .group(group)
                .build();

        guardianRepository.save(updatedGuardian);

        return convertToDTO(updatedGuardian);
    }

    @Transactional
    public void deleteGuardian(Long guardianId) {

        checkGuardianOwnership(guardianId);

        Guardian guardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> new GuardianNotFoundException("지도사 정보를 찾을 수 없습니다."));

        if (guardian.getImagePath() != null) {
            try {
                s3service.deleteImage(guardian.getImagePath(), "walkingschoolbus-bucket");
            } catch (Exception e) {
                log.error("이미지 삭제 실패 - 경로: {}, 에러 메시지: {}", guardian.getImagePath(), e.getMessage());
            }
        }

        guardianRepository.deleteById(guardianId);
    }


    private GuardianDTO convertToDTO(Guardian guardian) {
        Long groupId = guardian.getGroup() != null ? guardian.getGroup().getId() : null;
        return GuardianDTO.builder()
                .id(guardian.getId())
                .name(guardian.getName())
                .phone(guardian.getPhone())
                .bio(guardian.getBio())
                .experience(guardian.getExperience())
                .imagePath(guardian.getImagePath())
                .groupId(groupId)
                .build();
    }

    private String uploadImage(MultipartFile imageFile) {
        try {
            return s3service.uploadImageFile(imageFile, "walkingschoolbus-bucket");
        } catch (IOException e) {
            log.error("S3 업로드 실패 - 파일: {}, 에러 메시지: {}", imageFile.getOriginalFilename(), e.getMessage());
            throw new FileUploadException("이미지 업로드 실패", e);
        } catch (Exception e) {
            log.error("S3 서비스 오류 발생 - 파일: {}, 에러 메시지: {}", imageFile.getOriginalFilename(), e.getMessage());
            throw new FileUploadException("알 수 없는 오류로 이미지 업로드에 실패했습니다.", e);
        }
    }

    public GroupDTO getGuardianGroup() {
        Object userByContextHolder = userDetailsService.getUserByContextHolder();
        if (!(userByContextHolder instanceof Guardian)) {
            throw new GuardianNotFoundException("지도사 정보를 찾을 수 없습니다.");
        }

        Guardian guardian = (Guardian) userByContextHolder;
        Group group = guardian.getGroup();
        if (group == null) {
            throw new GroupNotFoundException("배정된 그룹을 찾을 수 없습니다.");
        }

        return GroupDTO.builder()
                .groupName(group.getGroupName())
                .schoolName(group.getSchoolName())
                .dutyGuardianId(group.getDutyGuardianId())
                .isGuideActive(group.getIsGuideActive())
                .id(group.getId())
                .build();
    }

    private void checkGuardianOwnership(Long guardianId) {
        Guardian loggedInGuardian = (Guardian) userDetailsService.getUserByContextHolder();

        if (!loggedInGuardian.getId().equals(guardianId)) {
            throw new GuardianNotAccessException("해당 지도사의 데이터를 수정할 권한이 없습니다.");
        }
    }


}

package com.ebiz.wsb.domain.guardian.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.group.dto.GroupDTO;
import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.exception.GroupNotFoundException;
import com.ebiz.wsb.domain.group.repository.GroupRepository;
import com.ebiz.wsb.domain.guardian.dto.GuardianDTO;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.exception.FileUploadException;
import com.ebiz.wsb.domain.guardian.exception.GuardianNotFoundException;
import com.ebiz.wsb.domain.guardian.repository.GuardianRepository;
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
        Guardian guardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> new GuardianNotFoundException("인솔자 정보를 찾을 수 없습니다."));
        return convertToDTO(guardian);
    }

    @Transactional
    public GuardianDTO updateGuardian(Long guardianId, GuardianDTO guardianDTO, MultipartFile imageFile) {
        Guardian existingGuardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> new GuardianNotFoundException("인솔자 정보를 찾을 수 없습니다."));

        Group group = existingGuardian.getGroup();

        if (guardianDTO.getGroupId() != null) {
            group = groupRepository.findById(guardianDTO.getGroupId())
                    .orElseThrow(() -> new GroupNotFoundException("해당 그룹을 찾을 수 없습니다."));
        }

        String imageUrl = existingGuardian.getImagePath();
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImage(imageFile);
        } else {
            imageUrl = existingGuardian.getImagePath();
        }

        Guardian updatedGuardian = Guardian.builder()
                .id(existingGuardian.getId())
                .name(existingGuardian.getName())
                .phone(existingGuardian.getPhone())
                .bio(guardianDTO.getBio() != null ? guardianDTO.getBio() : existingGuardian.getBio())  // bio 업데이트
                .experience(guardianDTO.getExperience() != null ? guardianDTO.getExperience() : existingGuardian.getExperience())  // experience 업데이트
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
        Guardian guardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> new GuardianNotFoundException("인솔자 정보를 찾을 수 없습니다."));

        if (guardian.getImagePath() != null) {
            s3service.deleteImage(guardian.getImagePath(), "walkingschoolbus-bucket");
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
            throw new FileUploadException("이미지 업로드 실패", e);
        }
    }

    public GroupDTO getGuardianGroup() {
        Object userByContextHolder = userDetailsService.getUserByContextHolder();
        if (userByContextHolder instanceof Guardian) {
            Guardian guardian = (Guardian) userByContextHolder;
            Group group = guardian.getGroup();
            return GroupDTO.builder()
                    .groupName(group.getGroupName())
                    .schoolName(group.getSchoolName())
                    .id(group.getId())
                    .build();
        } else {
            throw new GroupNotFoundException("배정된 그룹을 찾을 수 없습니다.");
        }
    }
}

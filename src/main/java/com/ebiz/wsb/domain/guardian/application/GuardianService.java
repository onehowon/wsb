package com.ebiz.wsb.domain.guardian.application;

import com.ebiz.wsb.domain.guardian.dto.GuardianDTO;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.exception.GuardianNotFoundException;
import com.ebiz.wsb.domain.guardian.repository.GuardianRepository;
import com.ebiz.wsb.domain.route.entity.Route;
import com.ebiz.wsb.domain.route.exception.RouteNotFoundException;
import com.ebiz.wsb.domain.route.repository.RouteRepository;
import com.ebiz.wsb.global.service.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuardianService {

    private final GuardianRepository guardianRepository;
    private final RouteRepository routeRepository;
    private final S3Service s3service;

    public List<GuardianDTO> getAllGuardians() {
        List<Guardian> guardians = guardianRepository.findAll();
        return guardians.stream().map(this::convertToDTO).toList();
    }

    public GuardianDTO getGuardianById(Long guardianId) {
        Guardian guardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> new GuardianNotFoundException("인솔자 정보를 찾을 수 없습니다."));
        return convertToDTO(guardian);
    }

    @Transactional
    public GuardianDTO updateGuardian(Long guardianId, GuardianDTO guardianDTO, MultipartFile imageFile) {
        Guardian existingGuardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> new GuardianNotFoundException("인솔자 정보를 찾을 수 없습니다."));

        String imageUrl = existingGuardian.getImagePath();
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImage(imageFile);
        }

        Route route = routeRepository.findById(guardianDTO.getRouteId())
                .orElseThrow(() -> new RouteNotFoundException("경로 정보를 찾을 수 없습니다."));

        Guardian updatedGuardian = Guardian.builder()
                .id(existingGuardian.getId())
                .name(guardianDTO.getName() != null ? guardianDTO.getName() : existingGuardian.getName())
                .email(guardianDTO.getEmail() != null ? guardianDTO.getEmail() : existingGuardian.getEmail())
                .phone(guardianDTO.getPhone() != null ? guardianDTO.getPhone() : existingGuardian.getPhone())
                .bio(guardianDTO.getBio() != null ? guardianDTO.getBio() : existingGuardian.getBio())
                .experience(guardianDTO.getExperience() != null ? guardianDTO.getExperience() : existingGuardian.getExperience())
                .imagePath(imageUrl != null ? imageUrl : existingGuardian.getImagePath())
                .route(route)
                .password(existingGuardian.getPassword())
                .build();

        guardianRepository.save(updatedGuardian);

        return convertToDTO(updatedGuardian);
    }


    @Transactional
    public GuardianDTO updateGuardianImage(Long guardianId, MultipartFile imageFile) {
        Guardian existingGuardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> new GuardianNotFoundException("인솔자 정보를 찾을 수 없습니다."));

        String newImagePath = uploadImage(imageFile);

        Guardian updatedGuardian = Guardian.builder()
                .id(existingGuardian.getId())
                .name(existingGuardian.getName())
                .email(existingGuardian.getEmail())
                .phone(existingGuardian.getPhone())
                .bio(existingGuardian.getBio())
                .experience(existingGuardian.getExperience())
                .imagePath(newImagePath)
                .route(existingGuardian.getRoute())
                .password(existingGuardian.getPassword())
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
        return GuardianDTO.builder()
                .id(guardian.getId())
                .name(guardian.getName())
                .email(guardian.getEmail())
                .phone(guardian.getPhone())
                .bio(guardian.getBio())
                .experience(guardian.getExperience())
                .imagePath(guardian.getImagePath())
                .routeId(guardian.getRoute() != null ? guardian.getRoute().getRouteId() : null)
                .password(guardian.getPassword())
                .build();
    }

    private String uploadImage(MultipartFile imageFile) {
        try {
            return s3service.uploadImageFile(imageFile, "walkingschoolbus-bucket");
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }
}

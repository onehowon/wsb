package com.ebiz.wsb.domain.guardian.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.group.dto.GroupDTO;
import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.exception.GroupNotFoundException;
import com.ebiz.wsb.domain.group.repository.GroupRepository;
import com.ebiz.wsb.domain.guardian.dto.GuardianDTO;
import com.ebiz.wsb.domain.guardian.dto.GuardianMapper;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.exception.FileUploadException;
import com.ebiz.wsb.domain.guardian.exception.GuardianNotAccessException;
import com.ebiz.wsb.domain.guardian.exception.GuardianNotFoundException;
import com.ebiz.wsb.domain.guardian.repository.GuardianRepository;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.waypoint.entity.Waypoint;
import com.ebiz.wsb.global.service.AuthorizationHelper;
import com.ebiz.wsb.global.service.ImageService;
import com.ebiz.wsb.global.service.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuardianService {

    private final GuardianRepository guardianRepository;
    private final AuthorizationHelper authorizationHelper;
    private final GuardianMapper guardianMapper;
    private final UserDetailsServiceImpl userDetailsService;
    private final ImageService imageService;
    private final S3Service s3Service;

    public GuardianDTO getMyGuardianInfo() {
        Guardian loggedInGuardian = authorizationHelper.getLoggedInGuardian();
        return guardianMapper.toDTO(loggedInGuardian);
    }

    @Transactional
    public List<GuardianDTO> getGuardiansForMyChild() {
        Object currentUser = userDetailsService.getUserByContextHolder();

        if (!(currentUser instanceof Parent parent)) {
            throw new GuardianNotAccessException("학부모만 지도사 정보를 조회할 수 있습니다.");
        }

        List<Long> childGroupIds = parent.getStudents().stream()
                .map(student -> student.getGroup().getId())
                .distinct()
                .collect(Collectors.toList());

        if (childGroupIds.isEmpty()) {
            throw new GroupNotFoundException("아이의 그룹 정보를 찾을 수 없습니다.");
        }

        List<Guardian> guardians = guardianRepository.findGuardiansByGroupId(childGroupIds.get(0));

        return guardians.stream()
                .map(guardianMapper::toDTO)
                .collect(Collectors.toList());
    }

    public GuardianDTO getGuardianById(Long guardianId) {
        Guardian guardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> new GuardianNotFoundException("지도사 정보를 찾을 수 없습니다."));

        Guardian loggedInGuardian = authorizationHelper.getLoggedInGuardian();
        if (!loggedInGuardian.getId().equals(guardianId)) {
            throw new GuardianNotAccessException("본인의 정보만 조회할 수 있습니다.");
        }

        return guardianMapper.toDTO(guardian);
    }

    @Transactional
    public void updateGuardianImageFile(MultipartFile imageFile) {
        Guardian loggedInGuardian = authorizationHelper.getLoggedInGuardian();

        String photoUrl = uploadImage(imageFile);

        Guardian updateGuardian = loggedInGuardian.toBuilder()
                .imagePath(photoUrl)
                .build();

        guardianRepository.save(updateGuardian);
    }

    @Transactional
    public void deleteMyGuardianInfo() {
        Guardian loggedInGuardian = authorizationHelper.getLoggedInGuardian();

        if (loggedInGuardian.getImagePath() != null) {
            try {
                imageService.deleteImage(loggedInGuardian.getImagePath(), "walkingschoolbus-bucket");
            } catch (Exception e) {
                log.error("이미지 삭제 실패 - 경로: {}, 에러 메시지: {}", loggedInGuardian.getImagePath(), e.getMessage());
            }
        }

        guardianRepository.deleteById(loggedInGuardian.getId());
    }

    public GroupDTO getGuardianGroup() {
        Guardian loggedInGuardian = authorizationHelper.getLoggedInGuardian();
        Group group = loggedInGuardian.getGroup();

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

    private String uploadImage(MultipartFile imageFile) {
        try {
            return s3Service.uploadImageFile(imageFile, "walkingschoolbus-bucket");
        } catch (IOException e) {
            throw new FileUploadException("이미지 업로드 실패", e);
        }
    }
}

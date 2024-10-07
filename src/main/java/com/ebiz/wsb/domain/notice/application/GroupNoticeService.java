package com.ebiz.wsb.domain.notice.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.guardian.dto.GuardianDTO;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.exception.FileUploadException;
import com.ebiz.wsb.domain.notice.dto.GroupNoticeDTO;
import com.ebiz.wsb.domain.notice.dto.NoticeTypeDTO;
import com.ebiz.wsb.domain.notice.entity.GroupNotice;
import com.ebiz.wsb.domain.notice.entity.NoticeType;
import com.ebiz.wsb.domain.notice.entity.NoticeTypeEnum;
import com.ebiz.wsb.domain.notice.exception.CustomInvalidNoticeTypeException;
import com.ebiz.wsb.domain.notice.exception.NoticeNotFoundException;
import com.ebiz.wsb.domain.notice.repository.GroupNoticeRepository;
import com.ebiz.wsb.domain.notice.repository.NoticeTypeRepository;
import com.ebiz.wsb.global.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupNoticeService {

    private final GroupNoticeRepository groupNoticeRepository;
    private final S3Service s3service;
    private final UserDetailsServiceImpl userDetailsService;
    private final NoticeTypeRepository noticeTypeRepository;

    public Page<GroupNoticeDTO> getAllGroupNotices(Pageable pageable) {
        return groupNoticeRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public GroupNoticeDTO getGroupNoticeById(Long groupNoticeId) {
        GroupNotice groupNotice = groupNoticeRepository.findById(groupNoticeId)
                .orElseThrow(() -> new NoticeNotFoundException(groupNoticeId));
        return convertToDTO(groupNotice);
    }


    public GroupNoticeDTO createGroupNotice(String noticeType, String content, MultipartFile imageFile, Authentication authentication) {

        NoticeType existingNoticeType = noticeTypeRepository.findByName(NoticeTypeEnum.valueOf(noticeType))
                .orElseThrow(() -> new CustomInvalidNoticeTypeException("유효하지 않은 공지 타입: " + noticeType));

        Guardian guardian = (Guardian) userDetailsService.getUserByContextHolder();

        String photoUrl = imageFile != null ? uploadImage(imageFile) : null;

        GroupNotice groupNotice = GroupNotice.builder()
                .noticeType(existingNoticeType)
                .guardian(guardian)
                .content(content)
                .photo(photoUrl)
                .likes(0)
                .createdAt(LocalDateTime.now())
                .build();

        GroupNotice savedGroupNotice = groupNoticeRepository.save(groupNotice);
        return convertToDTO(savedGroupNotice);
    }


    public GroupNoticeDTO updateGroupNotice(Long groupNoticeId, String content, MultipartFile imageFile) {
        return groupNoticeRepository.findById(groupNoticeId)
                .map(existingGroupNotice -> {
                    String updatedPhotoUrl = imageFile != null ? uploadImage(imageFile) : existingGroupNotice.getPhoto();

                    GroupNotice updatedGroupNotice = GroupNotice.builder()
                            .groupNoticeId(existingGroupNotice.getGroupNoticeId())
                            .noticeType(existingGroupNotice.getNoticeType())
                            .guardian(existingGroupNotice.getGuardian())
                            .content(content)
                            .photo(updatedPhotoUrl)
                            .likes(existingGroupNotice.getLikes())
                            .createdAt(existingGroupNotice.getCreatedAt())
                            .build();

                    GroupNotice savedGroupNotice = groupNoticeRepository.save(updatedGroupNotice);
                    return convertToDTO(savedGroupNotice);
                })
                .orElseThrow(() -> new NoticeNotFoundException(groupNoticeId));
    }

    public void deleteGroupNotice(Long groupNoticeId) {
        groupNoticeRepository.deleteById(groupNoticeId);
    }

    private GroupNoticeDTO convertToDTO(GroupNotice groupNotice) {
        return GroupNoticeDTO.builder()
                .noticeType(NoticeTypeDTO.builder()
                        .id(groupNotice.getNoticeType().getId())
                        .name(groupNotice.getNoticeType().getName().name())
                        .build())
                .content(groupNotice.getContent())
                .photo(groupNotice.getPhoto())  // S3 URL
                .build();
    }

    private String uploadImage(MultipartFile imageFile) {
        try {
            return s3service.uploadImageFile(imageFile, "walkingschoolbus-bucket");
        } catch (IOException e) {
            throw new FileUploadException("이미지 업로드 실패", e);
        }
    }
}
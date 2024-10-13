package com.ebiz.wsb.domain.notice.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.repository.GroupRepository;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.exception.FileUploadException;
import com.ebiz.wsb.domain.notice.dto.GroupNoticeDTO;
import com.ebiz.wsb.domain.notice.entity.GroupNotice;
import com.ebiz.wsb.domain.notice.exception.NoticeAccessDeniedException;
import com.ebiz.wsb.domain.notice.exception.NoticeNotFoundException;
import com.ebiz.wsb.domain.notice.repository.GroupNoticeRepository;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.global.service.S3Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupNoticeService {

    private final GroupNoticeRepository groupNoticeRepository;
    private final S3Service s3service;
    private final UserDetailsServiceImpl userDetailsService;
    private final GroupRepository groupRepository;

    public Page<GroupNoticeDTO> getAllGroupNotices(Pageable pageable) {
        return groupNoticeRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public GroupNoticeDTO getGroupNoticeById(Long groupNoticeId) {
        GroupNotice groupNotice = groupNoticeRepository.findById(groupNoticeId)
                .orElseThrow(() -> new NoticeNotFoundException(groupNoticeId));

        if(groupNotice.getGroup() == null){
            throw new IllegalStateException("공지사항에 연결된 그룹이 없습니다.");
        }

        Object currentUser = userDetailsService.getUserByContextHolder();

        boolean isMember = false;
        if (currentUser instanceof Guardian) {
            Guardian guardian = (Guardian) currentUser;

            if(guardian.getGroup().getId() == null){
                log.error("Guardian ID {}는 그룹에 속해 있지 않습니다..", guardian.getId());
                throw new NoticeAccessDeniedException("해당 인솔자는 그룹에 속해 있지 않습니다.");
            }

            isMember = groupRepository.isUserInGroupForGuardian(guardian.getId(), groupNotice.getGroup().getId());

        } else if (currentUser instanceof Parent) {
            Parent parent = (Parent) currentUser;

            if(parent.getGroup().getId() == null){
                log.error("Parent ID {}는 그룹에 속해 있지 않습니다..", parent.getId());
                throw new NoticeAccessDeniedException("해당 학부모는 그룹에 속해 있지 않습니다.");
            }
            isMember = groupRepository.isUserInGroupForParent(parent.getId(), groupNotice.getGroup().getId());
        }

        if(!isMember){
            throw new NoticeAccessDeniedException("공지사항 열람 권한이 없습니다.");
        }

        return convertToDTO(groupNotice);
    }


    public GroupNoticeDTO createGroupNotice(String content, MultipartFile imageFile, Authentication authentication) {

        Guardian guardian = (Guardian) userDetailsService.getUserByContextHolder();

        Group group = guardian.getGroup();

        String photoUrl = imageFile != null ? uploadImage(imageFile) : null;

        GroupNotice groupNotice = GroupNotice.builder()
                .guardian(guardian)
                .group(group)
                .content(content)
                .photo(photoUrl)
                .likes(0)
                .createdAt(LocalDateTime.now())
                .build();

        GroupNotice savedGroupNotice = groupNoticeRepository.save(groupNotice);
        return convertToDTO(savedGroupNotice);
    }


    @Transactional
    public GroupNoticeDTO updateGroupNotice(Long groupNoticeId, String content, MultipartFile imageFile) {
        return groupNoticeRepository.findById(groupNoticeId)
                .map(existingGroupNotice -> {
                    String updatedPhotoUrl = imageFile != null ? uploadImage(imageFile) : existingGroupNotice.getPhoto();

                    GroupNotice updatedGroupNotice = GroupNotice.builder()
                            .groupNoticeId(existingGroupNotice.getGroupNoticeId())
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
                .groupNoticeId(groupNotice.getGroupNoticeId())
                .content(groupNotice.getContent())
                .photo(groupNotice.getPhoto())
                .likes(groupNotice.getLikes())
                .createdAt(groupNotice.getCreatedAt())
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
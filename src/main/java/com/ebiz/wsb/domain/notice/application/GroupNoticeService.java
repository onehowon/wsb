package com.ebiz.wsb.domain.notice.application;

import com.ebiz.wsb.domain.auth.application.UserDetailsServiceImpl;
import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.exception.GroupNotFoundException;
import com.ebiz.wsb.domain.group.repository.GroupRepository;
import com.ebiz.wsb.domain.guardian.dto.GuardianSummaryDTO;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.exception.FileUploadException;
import com.ebiz.wsb.domain.notice.dto.GroupNoticeDTO;
import com.ebiz.wsb.domain.notice.entity.GroupNotice;
import com.ebiz.wsb.domain.notice.entity.GroupNoticePhoto;
import com.ebiz.wsb.domain.notice.exception.LikesNumberException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ebiz.wsb.domain.notice.entity.QGroupNotice.groupNotice;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupNoticeService {

    private final GroupNoticeRepository groupNoticeRepository;
    private final S3Service s3service;
    private final UserDetailsServiceImpl userDetailsService;
    private final GroupRepository groupRepository;

    @Transactional
    public Page<GroupNoticeDTO> getAllGroupNotices(Pageable pageable) {
        Page<GroupNotice> notices = groupNoticeRepository.findAllByOrderByCreatedAtDesc(pageable);
        return notices.map(this::convertToDTO);
    }

    @Transactional
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
                log.error("Guardian ID {}는 그룹에 속해 있지 않습니다.", guardian.getId());
                throw new NoticeAccessDeniedException("해당 인솔자는 그룹에 속해 있지 않습니다.");
            }

            isMember = groupRepository.isUserInGroupForGuardian(guardian.getId(), groupNotice.getGroup().getId());

        } else if (currentUser instanceof Parent) {
            Parent parent = (Parent) currentUser;

            if(parent.getGroup().getId() == null){
                log.error("Parent ID {}는 그룹에 속해 있지 않습니다.", parent.getId());
                throw new NoticeAccessDeniedException("해당 학부모는 그룹에 속해 있지 않습니다.");
            }
            isMember = groupRepository.isUserInGroupForParent(parent.getId(), groupNotice.getGroup().getId());
        }

        if(!isMember){
            throw new NoticeAccessDeniedException("공지사항 열람 권한이 없습니다.");
        }

        return convertToDTO(groupNotice);
    }


    public GroupNoticeDTO createGroupNotice(String content, List<MultipartFile> imageFiles, Authentication authentication) {

        Guardian guardian = (Guardian) userDetailsService.getUserByContextHolder();
        Group group = guardian.getGroup();

        if (group == null) {
            throw new GroupNotFoundException("인솔자가 그룹에 속해 있지 않습니다.");
        }

        GroupNotice groupNotice = GroupNotice.builder()
                .guardian(guardian)
                .group(group)
                .content(content)
                .likes(0)
                .createdAt(LocalDateTime.now())
                .photos(new ArrayList<>())
                .build();

        GroupNotice savedGroupNotice = groupNoticeRepository.save(groupNotice);

        List<GroupNoticePhoto> photoEntities = new ArrayList<>();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String photoUrl = uploadImage(file);
                    photoEntities.add(GroupNoticePhoto.builder()
                            .photoUrl(photoUrl)
                            .groupNotice(savedGroupNotice)
                            .build());
                }
            }
        }

        savedGroupNotice.getPhotos().addAll(photoEntities);

        groupNoticeRepository.save(savedGroupNotice);

        return convertToDTO(savedGroupNotice);
    }


    @Transactional
    public GroupNoticeDTO updateGroupNotice(Long groupNoticeId, String content, List<MultipartFile> imageFiles) {
        Guardian currentGuardian = (Guardian) userDetailsService.getUserByContextHolder();

        return groupNoticeRepository.findById(groupNoticeId)
                .map(existingGroupNotice -> {
                    if (!existingGroupNotice.getGuardian().getId().equals(currentGuardian.getId())) {
                        throw new NoticeAccessDeniedException("공지사항을 수정할 권한이 없습니다.");
                    }

                    List<GroupNoticePhoto> updatedPhotoEntities = new ArrayList<>();

                    if (imageFiles != null && !imageFiles.isEmpty()) {
                        for (MultipartFile file : imageFiles) {
                            if (!file.isEmpty()) {
                                String photoUrl = uploadImage(file);
                                updatedPhotoEntities.add(GroupNoticePhoto.builder()
                                        .photoUrl(photoUrl)
                                        .groupNotice(existingGroupNotice)
                                        .build());
                            }
                        }
                    }

                    GroupNotice updatedGroupNotice = GroupNotice.builder()
                            .groupNoticeId(existingGroupNotice.getGroupNoticeId())
                            .guardian(existingGroupNotice.getGuardian())
                            .group(existingGroupNotice.getGroup())
                            .content(content)
                            .photos(updatedPhotoEntities)
                            .likes(existingGroupNotice.getLikes())
                            .createdAt(existingGroupNotice.getCreatedAt())
                            .build();

                    GroupNotice savedGroupNotice = groupNoticeRepository.save(updatedGroupNotice);
                    return convertToDTO(savedGroupNotice);
                })
                .orElseThrow(() -> new NoticeNotFoundException(groupNoticeId));
    }

    public void deleteGroupNotice(Long groupNoticeId) {
        Guardian currentGuardian = (Guardian) userDetailsService.getUserByContextHolder();

        GroupNotice groupNotice = groupNoticeRepository.findById(groupNoticeId)
                .orElseThrow(() -> new NoticeNotFoundException(groupNoticeId));

        if (!groupNotice.getGuardian().getId().equals(currentGuardian.getId())) {
            throw new NoticeAccessDeniedException("공지사항을 삭제할 권한이 없습니다.");
        }

        groupNoticeRepository.deleteById(groupNoticeId);
    }

    private GroupNoticeDTO convertToDTO(GroupNotice groupNotice) {

        List<String> photoUrls = groupNotice.getPhotos().stream()
                .map(GroupNoticePhoto::getPhotoUrl)
                .collect(Collectors.toList());

        Guardian guardian = groupNotice.getGuardian();
        GuardianSummaryDTO guardianSummaryDTO = GuardianSummaryDTO.builder()
                .id(guardian.getId())
                .name(guardian.getName())
                .imagePath(guardian.getImagePath())
                .build();

        return GroupNoticeDTO.builder()
                .groupNoticeId(groupNotice.getGroupNoticeId())
                .content(groupNotice.getContent())
                .photos(photoUrls)
                .likes(groupNotice.getLikes())
                .createdAt(groupNotice.getCreatedAt())
                .guardian(guardianSummaryDTO)
                .build();
    }

    private String uploadImage(MultipartFile imageFile) {
        try {
            return s3service.uploadImageFile(imageFile, "walkingschoolbus-bucket");
        } catch (IOException e) {
            throw new FileUploadException("이미지 업로드 실패", e);
        }
    }

    @Transactional
    public String addLike(Long groupNoticeId) {
        GroupNotice groupNotice = groupNoticeRepository.findById(groupNoticeId)
                .orElseThrow(() -> new NoticeNotFoundException(groupNoticeId));

        GroupNotice updatedGroupNotice = GroupNotice.builder()
                .groupNoticeId(groupNotice.getGroupNoticeId())
                .guardian(groupNotice.getGuardian())
                .group(groupNotice.getGroup())
                .content(groupNotice.getContent())
                .photos(groupNotice.getPhotos())
                .likes(groupNotice.getLikes() + 1)
                .createdAt(groupNotice.getCreatedAt())
                .build();

        groupNoticeRepository.save(updatedGroupNotice);
        return "좋아요 +1";
    }

    @Transactional
    public String removeLike(Long groupNoticeId) {
        GroupNotice groupNotice = groupNoticeRepository.findById(groupNoticeId)
                .orElseThrow(() -> new NoticeNotFoundException(groupNoticeId));

        if (groupNotice.getLikes() > 0) {
            GroupNotice updatedGroupNotice = GroupNotice.builder()
                    .groupNoticeId(groupNotice.getGroupNoticeId())
                    .guardian(groupNotice.getGuardian())
                    .group(groupNotice.getGroup())
                    .content(groupNotice.getContent())
                    .photos(groupNotice.getPhotos())
                    .likes(groupNotice.getLikes() - 1)
                    .createdAt(groupNotice.getCreatedAt())
                    .build();

            groupNoticeRepository.save(updatedGroupNotice);
            return "좋아요 -1";
        } else {
            throw new LikesNumberException("좋아요는 0보다 작을 수 없습니다.");
        }
    }
}
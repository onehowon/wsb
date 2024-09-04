package com.ebiz.wsb.domain.notice.application;

import com.ebiz.wsb.domain.guardian.dto.GuardianDTO;
import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.notice.dto.GroupNoticeDTO;
import com.ebiz.wsb.domain.notice.dto.NoticeTypeDTO;
import com.ebiz.wsb.domain.notice.entity.GroupNotice;
import com.ebiz.wsb.domain.notice.entity.NoticeType;
import com.ebiz.wsb.domain.notice.entity.NoticeTypeEnum;
import com.ebiz.wsb.domain.notice.exception.CustomInvalidNoticeTypeException;
import com.ebiz.wsb.domain.notice.exception.NoticeNotFoundException;
import com.ebiz.wsb.domain.notice.repository.GroupNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupNoticeService {

    private final GroupNoticeRepository groupNoticeRepository;

    // 모든 그룹 공지 가져오기
    public List<GroupNoticeDTO> getAllGroupNotices() {
        return groupNoticeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 특정 그룹 공지 가져오기
    public GroupNoticeDTO getGroupNoticeById(Long groupNoticeId) {
        GroupNotice groupNotice = groupNoticeRepository.findById(groupNoticeId)
                .orElseThrow(() -> new NoticeNotFoundException(groupNoticeId));
        return convertToDTO(groupNotice);
    }


    // 새로운 그룹 공지 생성 (DTO에서 엔티티로 변환)
    public GroupNoticeDTO createGroupNotice(GroupNoticeDTO groupNoticeDTO) {
        GroupNotice groupNotice = convertToEntity(groupNoticeDTO);
        GroupNotice savedGroupNotice = groupNoticeRepository.save(groupNotice);
        return convertToDTO(savedGroupNotice);
    }

    // 기존 그룹 공지 수정
    public GroupNoticeDTO updateGroupNotice(Long groupNoticeId, GroupNoticeDTO updatedGroupNoticeDTO) {
        return groupNoticeRepository.findById(groupNoticeId)
                .map(existingGroupNotice -> {
                    GroupNotice updatedGroupNotice = GroupNotice.builder()
                            .groupNoticeId(existingGroupNotice.getGroupNoticeId())
                            .noticeType(existingGroupNotice.getNoticeType())
                            .guardian(existingGroupNotice.getGuardian())
                            .content(updatedGroupNoticeDTO.getContent())
                            .photo(updatedGroupNoticeDTO.getPhoto() != null ? updatedGroupNoticeDTO.getPhoto() : existingGroupNotice.getPhoto())
                            .likes(updatedGroupNoticeDTO.getLikes() != 0 ? updatedGroupNoticeDTO.getLikes() : existingGroupNotice.getLikes())
                            .createdAt(existingGroupNotice.getCreatedAt())
                            .build();
                    GroupNotice savedGroupNotice = groupNoticeRepository.save(updatedGroupNotice);
                    return convertToDTO(savedGroupNotice);
                })
                .orElseThrow(() -> new NoticeNotFoundException(groupNoticeId));
    }

    // 그룹 공지 삭제
    public void deleteGroupNotice(Long groupNoticeId) {
        groupNoticeRepository.deleteById(groupNoticeId);
    }

    // 엔티티를 DTO로 변환하는 메서드
    private GroupNoticeDTO convertToDTO(GroupNotice groupNotice) {
        return GroupNoticeDTO.builder()
                .groupNoticeId(groupNotice.getGroupNoticeId())
                .noticeType(NoticeTypeDTO.builder()
                        .id(groupNotice.getNoticeType().getId())
                        .name(groupNotice.getNoticeType().getName().name())
                        .build())
                .guardian(GuardianDTO.builder()
                        .id(groupNotice.getGuardian().getId())
                        .build())
                .content(groupNotice.getContent())
                .photo(groupNotice.getPhoto())
                .likes(groupNotice.getLikes())
                .createdAt(groupNotice.getCreatedAt() != null ? groupNotice.getCreatedAt().toString() : null)
                .build();
    }


    // DTO를 엔티티로 변환하는 메서드
    private GroupNotice convertToEntity(GroupNoticeDTO groupNoticeDTO) {
        NoticeTypeEnum noticeTypeEnum;
        try {
            noticeTypeEnum = NoticeTypeEnum.valueOf(groupNoticeDTO.getNoticeType().getName());
        } catch (IllegalArgumentException e) {
            throw new CustomInvalidNoticeTypeException("유효하지 않은 공지 타입: " + groupNoticeDTO.getNoticeType().getName());
        }

        return GroupNotice.builder()
                .groupNoticeId(groupNoticeDTO.getGroupNoticeId())
                .noticeType(groupNoticeDTO.getNoticeType() != null ?
                        NoticeType.builder()
                                .id(groupNoticeDTO.getNoticeType().getId())
                                .name(noticeTypeEnum)
                                .build() : null)
                .guardian(groupNoticeDTO.getGuardian() != null ?
                        Guardian.builder()
                                .id(groupNoticeDTO.getGuardian().getId())
                                .build() : null)
                .content(groupNoticeDTO.getContent())
                .photo(groupNoticeDTO.getPhoto())
                .likes(groupNoticeDTO.getLikes())
                .createdAt(groupNoticeDTO.getCreatedAt() != null ? LocalDateTime.parse(groupNoticeDTO.getCreatedAt()) : null)
                .build();
    }
}
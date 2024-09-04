package com.ebiz.wsb.domain.notice.application;

import com.ebiz.wsb.domain.notice.dto.NoticeDTO;
import com.ebiz.wsb.domain.notice.entity.Notice;
import com.ebiz.wsb.domain.notice.exception.NoticeNotFoundException;
import com.ebiz.wsb.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    public List<NoticeDTO> getAllNotices() {
        return noticeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public NoticeDTO getNoticeById(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));
        return convertToDTO(notice);
    }

    public NoticeDTO createNotice(NoticeDTO noticeDTO) {
        Notice notice = convertToEntity(noticeDTO);
        Notice savedNotice = noticeRepository.save(notice);
        return convertToDTO(savedNotice);
    }

    public NoticeDTO updateNotice(Long noticeId, NoticeDTO updatedNoticeDTO) {
        return noticeRepository.findById(noticeId)
                .map(existingNotice -> {
                    Notice updatedNotice = Notice.builder()
                            .noticeId(existingNotice.getNoticeId())
                            .title(updatedNoticeDTO.getTitle())
                            .content(updatedNoticeDTO.getContent())
                            .createdAt(existingNotice.getCreatedAt())
                            .build();
                    Notice savedNotice = noticeRepository.save(updatedNotice);
                    return convertToDTO(savedNotice);
                })
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));
    }

    public void deleteNotice(Long noticeId) {
        noticeRepository.deleteById(noticeId);
    }

    // 엔티티 -> DTO 변환 메서드
    private NoticeDTO convertToDTO(Notice notice) {
        return NoticeDTO.builder()
                .noticeId(notice.getNoticeId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt() != null ? notice.getCreatedAt().toString() : null)
                .build();
    }

    private Notice convertToEntity(NoticeDTO noticeDTO) {
        return Notice.builder()
                .noticeId(noticeDTO.getNoticeId())
                .title(noticeDTO.getTitle())
                .content(noticeDTO.getContent())
                .createdAt(noticeDTO.getCreatedAt() != null ? LocalDateTime.parse(noticeDTO.getCreatedAt()) : null)
                .build();
    }
}
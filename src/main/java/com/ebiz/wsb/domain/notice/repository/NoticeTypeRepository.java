package com.ebiz.wsb.domain.notice.repository;

import com.ebiz.wsb.domain.notice.entity.NoticeType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeTypeRepository extends JpaRepository<NoticeType, Long> {
}

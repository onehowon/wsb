package com.ebiz.wsb.domain.notice.repository;

import com.ebiz.wsb.domain.notice.entity.GroupNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupNoticeRepository extends JpaRepository<GroupNotice, Long> {
}

package com.ebiz.wsb.domain.notice.repository;

import com.ebiz.wsb.domain.notice.entity.GroupNotice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupNoticeRepository extends JpaRepository<GroupNotice, Long> {
}

package com.ebiz.wsb.domain.notice.repository;

import com.ebiz.wsb.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

}

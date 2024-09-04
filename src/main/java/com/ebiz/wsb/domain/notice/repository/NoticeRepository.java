package com.ebiz.wsb.domain.notice.repository;

import com.ebiz.wsb.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NoticeRepository extends JpaRepository<Notice, Long> {

}

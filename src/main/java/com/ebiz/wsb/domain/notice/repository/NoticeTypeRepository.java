package com.ebiz.wsb.domain.notice.repository;

import com.ebiz.wsb.domain.notice.entity.NoticeType;
import com.ebiz.wsb.domain.notice.entity.NoticeTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoticeTypeRepository extends JpaRepository<NoticeType, Long> {
    Optional<NoticeType> findByName(NoticeTypeEnum name);
}

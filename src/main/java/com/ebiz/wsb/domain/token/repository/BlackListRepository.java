package com.ebiz.wsb.domain.token.repository;

import com.ebiz.wsb.domain.token.entity.BlackList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackListRepository extends JpaRepository<BlackList, Long> {

}

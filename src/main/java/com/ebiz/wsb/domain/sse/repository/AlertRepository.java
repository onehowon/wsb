package com.ebiz.wsb.domain.sse.repository;

import com.ebiz.wsb.domain.sse.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

}

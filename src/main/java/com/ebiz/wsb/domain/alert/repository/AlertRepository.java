package com.ebiz.wsb.domain.alert.repository;

import com.ebiz.wsb.domain.alert.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByReceiverId(Long receiverId);
}

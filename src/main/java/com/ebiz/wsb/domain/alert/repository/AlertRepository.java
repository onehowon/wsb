package com.ebiz.wsb.domain.alert.repository;

import com.ebiz.wsb.domain.alert.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

}

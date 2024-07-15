package com.ebiz.wsb.repository;

import com.ebiz.wsb.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByUserIdOrderByTimestampDesc(Long userId);
}

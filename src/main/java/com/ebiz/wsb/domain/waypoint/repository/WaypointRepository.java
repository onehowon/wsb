package com.ebiz.wsb.domain.waypoint.repository;

import com.ebiz.wsb.domain.waypoint.entity.Waypoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WaypointRepository extends JpaRepository<Waypoint, Long> {
   List<Waypoint> findByRoute_Id(Long routeId);

}

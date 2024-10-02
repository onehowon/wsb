package com.ebiz.wsb.domain.route.repository;

import com.ebiz.wsb.domain.route.entity.GroupRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRouteRepository extends JpaRepository<GroupRoute, Long> {
}

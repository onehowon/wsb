package com.ebiz.wsb.domain.group.repository;

import com.ebiz.wsb.domain.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
}

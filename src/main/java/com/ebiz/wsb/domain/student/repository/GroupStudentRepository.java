package com.ebiz.wsb.domain.student.repository;

import com.ebiz.wsb.domain.student.entity.GroupStudent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupStudentRepository extends JpaRepository<GroupStudent, Long> {
}

package com.ebiz.wsb.domain.student.repository;

import com.ebiz.wsb.domain.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
}

package com.ebiz.wsb.domain.student.repository;

import com.ebiz.wsb.domain.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
}

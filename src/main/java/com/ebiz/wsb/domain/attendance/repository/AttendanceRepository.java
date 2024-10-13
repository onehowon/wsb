package com.ebiz.wsb.domain.attendance.repository;

import com.ebiz.wsb.domain.attendance.entity.Attendance;
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.waypoint.entity.Waypoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByStudentAndAttendanceDate(Student student, LocalDate attendanceDate);

    List<Attendance> findByWaypoint_IdAndAttendanceDate(Long waypointId, LocalDate attendanceDate);
}

package com.ebiz.wsb.domain.attendance.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long attendanceId;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "attendance_date")
    private LocalDateTime attendanceDate;

    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private AttendanceStatus status;

    @Column(name = "check_time")
    private Timestamp checkTime;
}

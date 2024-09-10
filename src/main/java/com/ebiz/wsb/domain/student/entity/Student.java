package com.ebiz.wsb.domain.student.entity;

import com.ebiz.wsb.domain.route.entity.Route;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
@Table(name = "StduentProfile")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "name")
    private String name;

    @Column(name = "guardian_contact")
    private String guardianContact;

    @ManyToOne
    @JoinColumn(name = "route_id", referencedColumnName = "route_id")
    private Route route;

    @Column(name = "school_name")
    private String schoolName;

    @Column(name = "grade")
    private String grade;

    @Column(name = "notes")
    private String notes;
}

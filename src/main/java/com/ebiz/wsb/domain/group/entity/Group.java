package com.ebiz.wsb.domain.group.entity;

import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.parent.entity.Parent;
import com.ebiz.wsb.domain.student.entity.Student;
import com.ebiz.wsb.domain.waypoint.entity.Waypoint;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
@Table(name = "`Group`")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    @Column(name = "school_name")
    private String schoolName;

    @Column(name = "group_name")
    private String groupName;

    @OneToMany(mappedBy = "group")
    private List<Guardian> guardians = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<Parent> parents = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<Student> students = new ArrayList<>();

    @OneToMany(mappedBy = "group")
    private List<Waypoint> waypoints = new ArrayList<>();
}

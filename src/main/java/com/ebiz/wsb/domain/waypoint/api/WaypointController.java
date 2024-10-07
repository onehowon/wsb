package com.ebiz.wsb.domain.waypoint.api;

import com.ebiz.wsb.domain.student.dto.StudentDTO;
import com.ebiz.wsb.domain.waypoint.application.WaypointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/waypoints")
public class WaypointController {

    private final WaypointService waypointService;

    @GetMapping("/{waypointId}/students")
    public ResponseEntity<List<StudentDTO>> getStudentsByWaypoint(@PathVariable Long waypointId) {
        List<StudentDTO> students = waypointService.getStudentByWaypoint(waypointId);
        return ResponseEntity.ok(students);
    }
}

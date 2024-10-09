package com.ebiz.wsb.domain.waypoint.api;

import com.ebiz.wsb.domain.student.dto.StudentDTO;
import com.ebiz.wsb.domain.waypoint.application.WaypointService;
import com.ebiz.wsb.domain.waypoint.dto.WaypointDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/waypoints")
public class WaypointController {

    private final WaypointService waypointService;

    @GetMapping
    public ResponseEntity<List<WaypointDTO>> getWaypoints() {
        List<WaypointDTO> waypoints = waypointService.getWaypoints();
        return ResponseEntity.ok(waypoints);
    }

    @GetMapping("/{waypointId}/students")
    public ResponseEntity<Map<String, Object>> getStudentsByWaypoint(@PathVariable Long waypointId) {
        List<StudentDTO> students = waypointService.getStudentByWaypoint(waypointId);
        Map<String, Object> response = new HashMap<>();
        response.put("students", students);
        response.put("totalStudentCount", students.size());
        return ResponseEntity.ok(response);
    }
}

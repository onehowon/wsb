package com.ebiz.wsb.domain.route.dto;

import com.ebiz.wsb.domain.guardian.dto.GuardianDTO;
import com.ebiz.wsb.domain.waypoint.dto.WaypointDTO;
import com.ebiz.wsb.domain.waypoint.entity.Waypoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouteDTO {
    private Long routeId;
    private String routeName;
    private List<WaypointDTO> waypoints;
    private List<GuardianDTO> guardians;
}

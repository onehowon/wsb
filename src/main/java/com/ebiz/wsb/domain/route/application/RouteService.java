package com.ebiz.wsb.domain.route.application;

import com.ebiz.wsb.domain.guardian.entity.Guardian;
import com.ebiz.wsb.domain.guardian.exception.GuardianNotFoundException;
import com.ebiz.wsb.domain.guardian.repository.GuardianRepository;
import com.ebiz.wsb.domain.route.dto.RouteDTO;
import com.ebiz.wsb.domain.route.entity.Route;
import com.ebiz.wsb.domain.route.exception.RouteNotFoundException;
import com.ebiz.wsb.domain.route.repository.RouteRepository;
import com.ebiz.wsb.domain.waypoint.dto.WaypointDTO;
import com.ebiz.wsb.domain.waypoint.entity.Waypoint;
import com.ebiz.wsb.domain.waypoint.repository.WaypointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteRepository routeRepository;
    private final GuardianRepository guardianRepository;
    private final WaypointRepository waypointRepository;

    public List<WaypointDTO> getWaypointsByGuardianId(Long guardianId) {
        Guardian guardian = guardianRepository.findById(guardianId)
                .orElseThrow(() -> new GuardianNotFoundException("인솔자를 찾을 수 없습니다."));

            Route route = guardian.getRoute();
            List<Waypoint> waypoints = waypointRepository.findByRoute_Id(route.getId());

            return waypoints.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
    }

    private WaypointDTO convertToDTO(Waypoint waypoint) {
        return WaypointDTO.builder()
                .waypointId(waypoint.getId())
                .waypointName(waypoint.getWaypointName())
                .latitude(waypoint.getLatitude())
                .longitude(waypoint.getLongitude())
                .waypointOrder(waypoint.getWaypointOrder())
                .routeId(waypoint.getRoute().getId()
                )
                .build();
    }


//    public List<RouteDTO> getAllRoutes(){
//        List<Route> routes = routeRepository.findAll();
//        return routes.stream().map(this::convertToDTO).toList();
//    }
//
//    public RouteDTO getRouteById(Long routeId){
//        Route route = routeRepository.findById(routeId)
//                .orElseThrow(() -> new RouteNotFoundException("해당 루트를 찾을 수 없습니다."));
//        return convertToDTO(route);
//    }

//    private RouteDTO convertToDTO(Route route) {
//        return RouteDTO.builder()
//                .routeId(route.getRouteId())
//                .routeName(route.getRouteName())
//                .latitude(route.getLatitude())
//                .longitude(route.getLongitude())
//                .routeOrder(route.getRouteOrder())
//                .build();
//    }

}

package com.ebiz.wsb.domain.route.application;

import com.ebiz.wsb.domain.route.dto.RouteDTO;
import com.ebiz.wsb.domain.route.entity.Route;
import com.ebiz.wsb.domain.route.exception.RouteNotFoundException;
import com.ebiz.wsb.domain.route.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final RouteRepository routeRepository;

    public List<RouteDTO> getAllRoutes(){
        List<Route> routes = routeRepository.findAll();
        return routes.stream().map(this::convertToDTO).toList();
    }

    public RouteDTO getRouteById(Long routeId){
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException("해당 루트를 찾을 수 없습니다."));
        return convertToDTO(route);
    }

    private RouteDTO convertToDTO(Route route) {
        return RouteDTO.builder()
                .route_id(route.getRoute_id())
                .route_name(route.getRoute_name())
                .latitude(route.getLatitude())
                .longitude(route.getLongitude())
                .route_order(route.getRoute_order())
                .build();
    }
}

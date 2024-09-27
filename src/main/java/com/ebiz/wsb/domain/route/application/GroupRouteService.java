package com.ebiz.wsb.domain.route.application;

import com.ebiz.wsb.domain.group.entity.Group;
import com.ebiz.wsb.domain.group.exception.GroupNotFoundException;
import com.ebiz.wsb.domain.group.repository.GroupRepository;
import com.ebiz.wsb.domain.route.entity.GroupRoute;
import com.ebiz.wsb.domain.route.entity.Route;
import com.ebiz.wsb.domain.route.exception.RouteNotFoundException;
import com.ebiz.wsb.domain.route.repository.GroupRouteRepository;
import com.ebiz.wsb.domain.route.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupRouteService {

    private final GroupRouteRepository groupRouteRepository;
    private final GroupRepository groupRepository;
    private final RouteRepository routeRepository;

    @Transactional
    public void assignRouteToGroup(Long groupId, Long routeId){
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("해당 그룹을 찾을 수 없습니다."));
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RouteNotFoundException("해당 경유지를 찾을 수 없습니다."));

        GroupRoute groupRoute = GroupRoute.builder()
                .group(group)
                .route(route)
                .build();

        groupRouteRepository.save(groupRoute);
    }
}

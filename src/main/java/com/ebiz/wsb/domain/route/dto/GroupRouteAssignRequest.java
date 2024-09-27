package com.ebiz.wsb.domain.route.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class GroupRouteAssignRequest {
    private Long routeId;
    private Long groupId;
}

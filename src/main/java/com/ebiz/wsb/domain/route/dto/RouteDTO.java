package com.ebiz.wsb.domain.route.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RouteDTO {
    private Long route_id;
    private String route_name;
    private Double latitude;
    private Double longitude;
    private Integer route_order;
}

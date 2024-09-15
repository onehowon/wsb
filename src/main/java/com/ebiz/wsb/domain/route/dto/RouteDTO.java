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
    private Long routeId;
    private String routeName;
    private Double latitude;
    private Double longitude;
    private Integer routeOrder;
}

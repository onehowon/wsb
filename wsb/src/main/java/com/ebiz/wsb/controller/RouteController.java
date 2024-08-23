package com.ebiz.wsb.controller;

import com.ebiz.wsb.model.Location;
import com.ebiz.wsb.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    @Autowired
    private RouteService routeService;

    // 경로 이탈 경고 API
    @PostMapping("/check")
    public ResponseEntity<?> checkRoute(@RequestBody Location location, @RequestParam double allowedDeviation){
        if (routeService.isOffRoute(location, allowedDeviation)){
            routeService.sendWarningAlert(location);
            return ResponseEntity.status(HttpStatus.OK).body("경로 이탈 경고");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body("경로 이탈 없음");
        }
    }
}

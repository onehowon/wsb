package com.ebiz.wsb.controller;

import com.ebiz.wsb.model.Location;
import com.ebiz.wsb.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    // 인솔자 실시간 위치 업로드 API
    @PostMapping("/upload")
    public ResponseEntity<?> uploadLocation(@RequestBody Location location){
        Location savedLocation = locationService.saveLocation(location);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLocation);
    }

    // 학부모가 인솔자의 실시간 위치를 조회하는 API
    @GetMapping("/user/{userid}")
    public ResponseEntity<?> getLocationByUserId(@PathVariable Long userId){
        List<Location> locations = locationService.getLocationsByUserId(userId);
        return ResponseEntity.ok(locations);
    }
}

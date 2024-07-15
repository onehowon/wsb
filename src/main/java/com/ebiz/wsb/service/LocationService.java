package com.ebiz.wsb.service;

import com.ebiz.wsb.model.Location;
import com.ebiz.wsb.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocationService {
    @Autowired
    private LocationRepository locationRepository;

    public Location saveLocation(Location location){
        return locationRepository.save(location);
    }

    public List<Location> getLocationsByUserId(Long userId){
        return locationRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}

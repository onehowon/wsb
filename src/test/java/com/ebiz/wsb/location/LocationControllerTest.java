package com.ebiz.wsb.location;

import com.ebiz.wsb.domain.location.api.LocationController;
import com.ebiz.wsb.domain.location.dto.LocationDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;


@SpringJUnitConfig
public class LocationControllerTest {

    @InjectMocks
    private LocationController locationController;

    @BeforeEach
    void setUp() {
        // Mockito 초기화
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendLocation() {
        // LocationDTO 생성 시 double 값을 사용
        LocationDTO location = LocationDTO.builder()
                .latitude(37.7749)
                .longitude(-122.4194)
                .build();

        LocationDTO result = locationController.sendLocation(location);

        Assertions.assertEquals(location.getLongitude(), result.getLongitude(), 0.001);
        Assertions.assertEquals(location.getLatitude(), result.getLatitude(), 0.001);

    }
}

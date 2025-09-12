package com.roomify.core.service;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class AvailabilityServiceTest {

    private final AvailabilityService availabilityService = new AvailabilityService();

    @Test
    void isAvailable_returnsTrue() {
        // Current skeleton implementation always returns true
        boolean result = availabilityService.isAvailable("room1", LocalDate.now(), LocalDate.now().plusDays(1));
        assertTrue(result);
    }

    @Test
    void isAvailable_withDifferentRoom_returnsTrue() {
        boolean result = availabilityService.isAvailable("room2", LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 3));
        assertTrue(result);
    }

    @Test
    void isAvailable_withLongerStay_returnsTrue() {
        boolean result = availabilityService.isAvailable("room3", LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 10));
        assertTrue(result);
    }
}
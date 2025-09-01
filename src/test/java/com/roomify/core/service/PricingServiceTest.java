package com.roomify.core.service;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PricingServiceTest {
    @Test
    void calculatePrice_nightsTimesBase() {
        PricingService pricing = new PricingService();
        double price = pricing.calculatePrice("r1", LocalDate.of(2025,1,1), LocalDate.of(2025,1,4));
        assertEquals(300.0, price);
    }
}

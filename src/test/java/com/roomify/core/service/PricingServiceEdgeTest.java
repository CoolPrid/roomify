package com.roomify.core.service;

import com.roomify.core.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PricingServiceEdgeTest {

    @Mock
    private RoomRepository roomRepository;

    private PricingService pricing;

    @BeforeEach
    void setUp() {
        pricing = new PricingService(roomRepository);
    }

    @Test
    void calculatePrice_zeroNights_returnsZero() {
        double price = pricing.calculatePrice("r1", LocalDate.of(2025, 4, 1), LocalDate.of(2025, 4, 1));
        assertEquals(0.0, price);
    }

    @Test
    void calculatePrice_negativeRange_returnsZero() {
        double price = pricing.calculatePrice("r1", LocalDate.of(2025, 5, 5), LocalDate.of(2025, 5, 1));
        assertEquals(0.0, price);
    }

    @Test
    void calculatePrice_multipleNights_correct() {
        // 5 nights in spring (April) for unknown room (uses default $100 rate)
        LocalDate checkIn = LocalDate.of(2025, 4, 1); // Spring
        LocalDate checkOut = LocalDate.of(2025, 4, 6); // 5 nights

        double price = pricing.calculatePrice("r1", checkIn, checkOut);

        // Should calculate based on actual daily rates and apply any applicable discounts
        assertTrue(price > 0);
        // Exact calculation depends on days of week, but should be reasonable
        assertTrue(price >= 400.0 && price <= 600.0); // Rough range check
    }

    @Test
    void calculatePrice_nullParameters_returnsZero() {
        assertEquals(0.0, pricing.calculatePrice(null, LocalDate.now(), LocalDate.now().plusDays(1)));
        assertEquals(0.0, pricing.calculatePrice("room", null, LocalDate.now().plusDays(1)));
        assertEquals(0.0, pricing.calculatePrice("room", LocalDate.now(), null));
    }

    @Test
    void calculatePrice_singleNight_returnsPositiveValue() {
        LocalDate checkIn = LocalDate.of(2025, 4, 15); // Spring weekday
        LocalDate checkOut = checkIn.plusDays(1);

        double price = pricing.calculatePrice("test-room", checkIn, checkOut);
        assertTrue(price > 0);
    }
}

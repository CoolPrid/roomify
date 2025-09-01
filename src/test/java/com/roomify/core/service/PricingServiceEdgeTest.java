package com.roomify.core.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PricingServiceEdgeTest {

    private final PricingService pricing = new PricingService();

    @Test
    void calculatePrice_zeroNights_returnsZero() {
        double price = pricing.calculatePrice("r1", LocalDate.of(2025,4,1), LocalDate.of(2025,4,1));
        assertEquals(0.0, price);
    }

    @Test
    void calculatePrice_negativeRange_returnsZero() {
        double price = pricing.calculatePrice("r1", LocalDate.of(2025,5,5), LocalDate.of(2025,5,1));
        assertEquals(0.0, price);
    }

    @Test
    void calculatePrice_multipleNights_correct() {
        double price = pricing.calculatePrice("r1", LocalDate.of(2025,6,1), LocalDate.of(2025,6,6));
        assertEquals(5 * 100.0, price);
    }
}

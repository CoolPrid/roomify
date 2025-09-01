package com.roomify.core.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiscountServiceEdgeTest {

    private final DiscountService discountService = new DiscountService();

    @Test
    void applyDiscount_baseCase_returnsBasePrice() {
        double out = discountService.applyDiscount("u1", 150.0);
        assertEquals(150.0, out);
    }

    @Test
    void applyDiscount_zeroPrice_returnsZero() {
        double out = discountService.applyDiscount("u1", 0.0);
        assertEquals(0.0, out);
    }
}

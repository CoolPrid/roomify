package com.roomify.core.service;

import com.roomify.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceEdgeTest {

    @Mock
    private UserRepository userRepository;

    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        discountService = new DiscountService(userRepository);
    }

    @Test
    void applyDiscount_baseCase_returnsBasePrice() {
        // Regular user (not VIP, not first-time) should get no discount
        double out = discountService.applyDiscount("regular-user", 150.0);
        assertEquals(150.0, out);
    }

    @Test
    void applyDiscount_zeroPrice_returnsZero() {
        double out = discountService.applyDiscount("any-user", 0.0);
        assertEquals(0.0, out);
    }

    @Test
    void applyDiscount_negativePrice_returnsZero() {
        // Enhanced service now handles negative prices correctly
        double out = discountService.applyDiscount("any-user", -50.0);
        assertEquals(0.0, out);
    }

    @Test
    void applyDiscount_verySmallPrice_respectsMinimumFloor() {
        // Even with discounts, price should never go below $10 minimum
        double out = discountService.applyDiscount("vip-user-1", 5.0);
        assertEquals(10.0, out); // Minimum floor
    }
}

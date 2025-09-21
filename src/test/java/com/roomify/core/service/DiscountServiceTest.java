package com.roomify.core.service;

import com.roomify.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    private UserRepository userRepository;

    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        discountService = new DiscountService(userRepository);
    }

    @Test
    void applyDiscount_regularUser_noDiscount() {
        double result = discountService.applyDiscount("regular-user", 100.0);
        assertEquals(100.0, result);
    }

    @Test
    void applyDiscount_zeroPrice_returnsZero() {
        double result = discountService.applyDiscount("any-user", 0.0);
        assertEquals(0.0, result);
    }

    @Test
    void applyDiscount_negativePrice_returnsZero() {
        double result = discountService.applyDiscount("any-user", -50.0);
        assertEquals(0.0, result);
    }

    @Test
    void applyDiscount_vipCustomer_gets10PercentOff() {
        double result = discountService.applyDiscount("vip-user-1", 100.0);
        assertEquals(90.0, result); // 10% off
    }

    @Test
    void applyDiscount_firstTimeCustomer_gets5PercentOff() {
        double result = discountService.applyDiscount("new-customer-123", 100.0);
        assertEquals(95.0, result); // 5% off
    }

    @Test
    void applyDiscount_validPromoCode_appliesCorrectDiscount() {
        assertEquals(90.0, discountService.applyDiscount("user", 100.0, "WELCOME10")); // 10% off
        assertEquals(80.0, discountService.applyDiscount("user", 100.0, "SAVE20"));    // 20% off
        assertEquals(75.0, discountService.applyDiscount("user", 100.0, "SUMMER25"));  // 25% off
    }

    @Test
    void applyDiscount_invalidPromoCode_noDiscount() {
        double result = discountService.applyDiscount("user", 100.0, "INVALID_CODE");
        assertEquals(100.0, result);
    }

    @Test
    void applyDiscount_vipCustomerWithPromo_stacksDiscounts() {
        // VIP (10% off) + WELCOME10 (10% off) = 19% off total
        // 100 * 0.9 * 0.9 = 81
        double result = discountService.applyDiscount("vip-user-1", 100.0, "WELCOME10");
        assertEquals(81.0, result);
    }

    @Test
    void applyDiscount_longStay_gets15PercentOff() {
        LocalDate checkIn = LocalDate.of(2025, 6, 1);
        LocalDate checkOut = LocalDate.of(2025, 6, 8); // 7 nights

        double result = discountService.applyDiscount("user", 100.0, null, checkIn, checkOut);
        assertEquals(85.0, result); // 15% off for 7+ nights
    }

    @Test
    void applyDiscount_multipleDiscounts_cappedAt60Percent() {
        // Try to stack many discounts that would exceed 60%
        discountService.addVipUser("new-super-customer");
        discountService.addPromoCode("MEGA50", 0.50); // 50% off promo

        LocalDate friday = LocalDate.of(2025, 6, 6);
        LocalDate longStay = friday.plusDays(10); // Long stay + weekend

        // This would be many discounts but should be capped at 60% max
        double result = discountService.applyDiscount("new-super-customer", 100.0, "MEGA50", friday, longStay);
        assertEquals(40.0, result); // 60% off max = $40 remaining
    }

    @Test
    void applyDiscount_lowPrice_hasMinimumFloor() {
        // Even with big discounts, price should never go below $10
        discountService.addPromoCode("HUGE90", 0.90); // 90% off

        double result = discountService.applyDiscount("vip-user-1", 15.0, "HUGE90");
        assertEquals(10.0, result); // Floor at $10
    }
}

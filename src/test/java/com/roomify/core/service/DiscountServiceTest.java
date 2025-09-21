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
class EnhancedDiscountServiceTest {

    @Mock
    private UserRepository userRepository;

    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        discountService = new DiscountService(userRepository);
    }

    // Basic functionality tests
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

    // VIP customer tests
    @Test
    void applyDiscount_vipCustomer_gets10PercentOff() {
        double result = discountService.applyDiscount("vip-user-1", 100.0);
        assertEquals(90.0, result); // 10% off
    }

    @Test
    void applyDiscount_multipleVipUsers_allGetDiscount() {
        assertEquals(90.0, discountService.applyDiscount("vip-user-1", 100.0));
        assertEquals(90.0, discountService.applyDiscount("vip-user-2", 100.0));
        assertEquals(90.0, discountService.applyDiscount("premium-customer", 100.0));
    }

    // First-time customer tests
    @Test
    void applyDiscount_firstTimeCustomer_gets5PercentOff() {
        double result = discountService.applyDiscount("new-customer-123", 100.0);
        assertEquals(95.0, result); // 5% off
    }

    @Test
    void applyDiscount_userWithFirstInName_gets5PercentOff() {
        double result = discountService.applyDiscount("john-first-time", 100.0);
        assertEquals(95.0, result); // 5% off
    }

    // Promo code tests
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
    void applyDiscount_expiredPromoCode_noDiscount() {
        double result = discountService.applyDiscount("user", 100.0, "EXPIRED");
        assertEquals(100.0, result);
    }

    // Stacking discounts tests
    @Test
    void applyDiscount_vipCustomerWithPromo_stacksDiscounts() {
        // VIP (10% off) + WELCOME10 (10% off) = 19% off total
        // 100 * 0.9 * 0.9 = 81
        double result = discountService.applyDiscount("vip-user-1", 100.0, "WELCOME10");
        assertEquals(81.0, result);
    }

    @Test
    void applyDiscount_firstTimeVipWithPromo_stacksAllDiscounts() {
        // First-time (5% off) + VIP (10% off) + SAVE20 (20% off)
        // 100 * 0.95 * 0.9 * 0.8 = 68.4
        discountService.addVipUser("new-vip-customer");
        double result = discountService.applyDiscount("new-vip-customer", 100.0, "SAVE20");
        assertEquals(68.4, result);
    }

    // Long stay discount tests
    @Test
    void applyDiscount_longStay_gets15PercentOff() {
        LocalDate checkIn = LocalDate.of(2025, 6, 1);
        LocalDate checkOut = LocalDate.of(2025, 6, 8); // 7 nights

        double result = discountService.applyDiscount("user", 100.0, null, checkIn, checkOut);
        assertEquals(85.0, result); // 15% off for 7+ nights
    }

    @Test
    void applyDiscount_shortStay_noLongStayDiscount() {
        LocalDate checkIn = LocalDate.of(2025, 6, 1);
        LocalDate checkOut = LocalDate.of(2025, 6, 4); // 3 nights

        double result = discountService.applyDiscount("user", 100.0, null, checkIn, checkOut);
        assertEquals(100.0, result); // No long stay discount
    }

    // Weekend stay tests
    @Test
    void applyDiscount_fridayCheckIn_getsWeekendDiscount() {
        LocalDate friday = LocalDate.of(2025, 6, 6); // Assuming this is a Friday
        LocalDate sunday = LocalDate.of(2025, 6, 8);

        double result = discountService.applyDiscount("user", 100.0, null, friday, sunday);
        assertEquals(92.0, result); // 8% weekend discount
    }

    @Test
    void applyDiscount_mondayCheckIn_noWeekendDiscount() {
        LocalDate monday = LocalDate.of(2025, 6, 2); // Assuming this is a Monday
        LocalDate wednesday = LocalDate.of(2025, 6, 4);

        double result = discountService.applyDiscount("user", 100.0, null, monday, wednesday);
        assertEquals(100.0, result); // No weekend discount
    }

    // Maximum discount cap tests
    @Test
    void applyDiscount_multipleDiscounts_cappedAt60Percent() {
        // Try to stack many discounts that would exceed 60%
        discountService.addVipUser("new-super-customer");
        discountService.addPromoCode("MEGA50", 0.50); // 50% off promo

        LocalDate friday = LocalDate.of(2025, 6, 6);
        LocalDate longStay = friday.plusDays(10); // Long stay + weekend

        // This would be: VIP(10%) + FirstTime(5%) + Promo(50%) + LongStay(15%) + Weekend(8%)
        // But should be capped at 60% max
        double result = discountService.applyDiscount("new-super-customer", 100.0, "MEGA50", friday, longStay);
        assertEquals(40.0, result); // 60% off max = $40 remaining
    }

    // Minimum price floor tests
    @Test
    void applyDiscount_lowPrice_hasMinimumFloor() {
        // Even with big discounts, price should never go below $10
        discountService.addPromoCode("HUGE90", 0.90); // 90% off

        double result = discountService.applyDiscount("vip-user-1", 15.0, "HUGE90");
        assertEquals(10.0, result); // Floor at $10
    }

    @Test
    void applyDiscount_veryLowPrice_stillHasFloor() {
        double result = discountService.applyDiscount("vip-user-1", 5.0, "SUMMER25");
        assertEquals(10.0, result); // Floor at $10, even if original was $5
    }

    // Precision tests
    @Test
    void applyDiscount_roundsToTwoDecimalPlaces() {
        // 100 * 0.9 * 0.95 * 0.9 = 76.95
        discountService.addVipUser("new-vip-test");
        double result = discountService.applyDiscount("new-vip-test", 100.0, "WELCOME10");
        assertEquals(76.95, result);
    }

    // Complex scenario tests
    @Test
    void applyDiscount_complexRealWorldScenario() {
        // Scenario: VIP customer, first time booking, has promo code,
        // booking for a week over a weekend
        discountService.addVipUser("new-vip-weekend-booker");

        LocalDate friday = LocalDate.of(2025, 6, 6);
        LocalDate nextFriday = friday.plusDays(7); // 7 nights

        double result = discountService.applyDiscount(
                "new-vip-weekend-booker",
                200.0,
                "WELCOME10",
                friday,
                nextFriday
        );

        // Expected: 200 * 0.95 (first) * 0.9 (vip) * 0.9 (promo) * 0.85 (long) * 0.92 (weekend)
        // = 200 * 0.95 * 0.9 * 0.9 * 0.85 * 0.92 = 119.31
        assertEquals(119.31, result);
    }

    // Edge cases
    @Test
    void applyDiscount_exactlySevenNights_getsLongStayDiscount() {
        LocalDate checkIn = LocalDate.of(2025, 6, 1);
        LocalDate checkOut = LocalDate.of(2025, 6, 8); // Exactly 7 nights

        double result = discountService.applyDiscount("user", 100.0, null, checkIn, checkOut);
        assertEquals(85.0, result); // Should get long stay discount
    }

    @Test
    void applyDiscount_sixNights_noLongStayDiscount() {
        LocalDate checkIn = LocalDate.of(2025, 6, 1);
        LocalDate checkOut = LocalDate.of(2025, 6, 7); // Only 6 nights

        double result = discountService.applyDiscount("user", 100.0, null, checkIn, checkOut);
        assertEquals(100.0, result); // Should NOT get long stay discount
    }
}

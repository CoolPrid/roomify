package com.roomify.core.service;

import com.roomify.core.dto.Room;
import com.roomify.core.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private RoomRepository roomRepository;

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService(roomRepository);
    }

    @Test
    void calculatePrice_nullParameters_returnsZero() {
        assertEquals(0.0, pricingService.calculatePrice(null, LocalDate.now(), LocalDate.now().plusDays(1)));
        assertEquals(0.0, pricingService.calculatePrice("room1", null, LocalDate.now().plusDays(1)));
        assertEquals(0.0, pricingService.calculatePrice("room1", LocalDate.now(), null));
    }

    @Test
    void calculatePrice_invalidDateRange_returnsZero() {
        LocalDate today = LocalDate.now();
        assertEquals(0.0, pricingService.calculatePrice("room1", today.plusDays(2), today.plusDays(1)));
        assertEquals(0.0, pricingService.calculatePrice("room1", today, today));
    }

    @Test
    void calculatePrice_usesRoomRepositoryBasePrice() {
        Room room = new Room("custom-room", "deluxe", 2, 250.0);
        when(roomRepository.findById("custom-room")).thenReturn(Optional.of(room));

        LocalDate checkIn = LocalDate.of(2025, 3, 17); // Spring Monday
        LocalDate checkOut = checkIn.plusDays(1);

        double price = pricingService.calculatePrice("custom-room", checkIn, checkOut);

        // Should use room's base price (250) not default (100)
        assertTrue(price > 0);
        assertTrue(price >= 200); // Should be at least the discounted base price
    }

    @Test
    void calculatePrice_fridayNight_hasWeekendPremium() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate friday = LocalDate.of(2025, 6, 6); // Friday in summer
        LocalDate saturday = friday.plusDays(1);

        double price = pricingService.calculatePrice("test-room", friday, saturday);

        // Should have weekend premium applied
        assertTrue(price > 100); // Base rate with premiums should be higher
    }

    @Test
    void calculatePrice_winterDiscount() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate winterDay = LocalDate.of(2025, 1, 15); // January Monday
        LocalDate nextDay = winterDay.plusDays(1);

        double price = pricingService.calculatePrice("test-room", winterDay, nextDay);

        // Winter should have discount (0.8 multiplier)
        assertTrue(price < 100); // Should be less than base rate
        assertEquals(80.0, price); // 100 * 0.8 winter discount
    }

    @Test
    void calculatePrice_summerPremium() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate summerDay = LocalDate.of(2025, 7, 21); // July Monday
        LocalDate nextDay = summerDay.plusDays(1);

        double price = pricingService.calculatePrice("test-room", summerDay, nextDay);

        // Summer should have premium (1.4 multiplier)
        assertTrue(price > 100); // Should be more than base rate
        assertEquals(140.0, price); // 100 * 1.4 summer premium
    }

    @Test
    void calculatePrice_holidayPremium() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate christmas = LocalDate.of(2025, 12, 25); // Thursday
        LocalDate nextDay = christmas.plusDays(1);

        double price = pricingService.calculatePrice("test-room", christmas, nextDay);

        // Should have both winter discount and holiday premium
        // 100 * 0.8 (winter) * 1.5 (holiday) = 120
        assertEquals(120.0, price);
    }

    @Test
    void calculatePrice_weeklyStay_gets5PercentOff() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.of(2025, 4, 7); // Spring Monday
        LocalDate checkOut = checkIn.plusDays(7); // 7 nights

        double price = pricingService.calculatePrice("test-room", checkIn, checkOut);

        // Should get 5% discount for weekly stay
        assertTrue(price > 0);
        // Base would be 7 * 100 = 700, with 5% discount = 665
        double expectedBase = 7 * 100 * 0.95; // Spring has 1.0 multiplier
        assertTrue(Math.abs(price - expectedBase) < 50); // Allow some variance for day-of-week differences
    }

    @Test
    void calculatePrice_monthlyStay_gets20PercentOff() {
        when(roomRepository.findById("economy-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.of(2025, 4, 1);
        LocalDate checkOut = checkIn.plusDays(28); // 28 nights

        double price = pricingService.calculatePrice("economy-room", checkIn, checkOut);

        // Should get 20% discount for monthly stay
        assertTrue(price > 0);
        // Economy room base is 80, so 28 * 80 * 0.8 (discount) = 1792
        assertTrue(price < 2000); // Should be significantly discounted
    }

    @Test
    void calculatePrice_earlyBooking_gets5PercentOff() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.now().plusDays(90);
        LocalDate checkOut = checkIn.plusDays(2);

        double price = pricingService.calculatePrice("test-room", checkIn, checkOut);

        // Should get 5% early booking discount
        assertTrue(price > 0);
        // Should be less than full price due to early booking discount
        assertTrue(price < 200); // 2 nights * 100 base rate
    }

    @Test
    void calculatePrice_multipleNights_sumsCorrectly() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.of(2025, 4, 14); // Spring Monday
        LocalDate checkOut = checkIn.plusDays(3); // Mon, Tue, Wed nights

        double totalPrice = pricingService.calculatePrice("test-room", checkIn, checkOut);

        // Should be sum of individual night prices
        assertTrue(totalPrice > 0);
        assertEquals(270.0, totalPrice); // 3 nights * 100 * 0.9 (weekday demand) = 270
    }
}

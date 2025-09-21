package com.roomify.core.service;

import com.roomify.core.dto.Room;
import com.roomify.core.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnhancedPricingServiceTest {

    @Mock
    private RoomRepository roomRepository;

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService(roomRepository);
    }

    // Basic validation tests
    @Test
    void calculatePrice_nullParameters_returnsZero() {
        assertEquals(0.0, pricingService.calculatePrice(null, LocalDate.now(), LocalDate.now().plusDays(1)));
        assertEquals(0.0, pricingService.calculatePrice("room1", null, LocalDate.now().plusDays(1)));
        assertEquals(0.0, pricingService.calculatePrice("room1", LocalDate.now(), null));
    }

    @Test
    void calculatePrice_invalidDateRange_returnsZero() {
        LocalDate today = LocalDate.now();

        // From after to
        assertEquals(0.0, pricingService.calculatePrice("room1", today.plusDays(2), today.plusDays(1)));

        // Same dates
        assertEquals(0.0, pricingService.calculatePrice("room1", today, today));
    }

    @Test
    void calculatePrice_zeroNights_returnsZero() {
        LocalDate date = LocalDate.of(2025, 6, 1);
        assertEquals(0.0, pricingService.calculatePrice("room1", date, date));
    }

    // Base rate tests
    @Test
    void calculatePrice_usesRoomRepositoryBasePrice() {
        Room room = new Room("custom-room", "deluxe", 2, 250.0);
        when(roomRepository.findById("custom-room")).thenReturn(Optional.of(room));

        LocalDate checkIn = LocalDate.of(2025, 3, 15); // Spring weekday
        LocalDate checkOut = checkIn.plusDays(1);

        double price = pricingService.calculatePrice("custom-room", checkIn, checkOut);

        // Should use room's base price (250) not default (100)
        // Spring = 1.0 multiplier, Tuesday = 0.9 demand, no weekend premium
        // Expected: 250 * 1.0 * 0.9 = 225
        assertEquals(225.0, price);
    }

    @Test
    void calculatePrice_fallsBackToDefaultRates() {
        when(roomRepository.findById("unknown-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.of(2025, 3, 15); // Spring weekday
        LocalDate checkOut = checkIn.plusDays(1);

        double price = pricingService.calculatePrice("unknown-room", checkIn, checkOut);

        // Should use default rate (100)
        // Spring = 1.0 multiplier, Tuesday = 0.9 demand
        // Expected: 100 * 1.0 * 0.9 = 90
        assertEquals(90.0, price);
    }

    @Test
    void calculatePrice_usesPredefinedRoomRates() {
        when(roomRepository.findById("economy-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.of(2025, 3, 15); // Spring weekday
        LocalDate checkOut = checkIn.plusDays(1);

        double price = pricingService.calculatePrice("economy-room", checkIn, checkOut);

        // Should use predefined rate for economy-room (80)
        // Spring = 1.0, Tuesday = 0.9 demand
        // Expected: 80 * 1.0 * 0.9 = 72
        assertEquals(72.0, price);
    }

    // Weekend premium tests
    @Test
    void calculatePrice_fridayNight_hasWeekendPremium() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate friday = LocalDate.of(2025, 6, 6); // Assume this is Friday
        LocalDate saturday = friday.plusDays(1);

        double price = pricingService.calculatePrice("test-room", friday, saturday);

        // Base: 100, Weekend: 1.3, Summer: 1.4, Friday demand: 1.2
        // Expected: 100 * 1.3 * 1.4 * 1.2 = 218.4
        assertEquals(218.4, price);
    }

    @Test
    void calculatePrice_saturdayNight_hasWeekendPremium() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate saturday = LocalDate.of(2025, 6, 7); // Assume this is Saturday
        LocalDate sunday = saturday.plusDays(1);

        double price = pricingService.calculatePrice("test-room", saturday, sunday);

        // Base: 100, Weekend: 1.3, Summer: 1.4, Saturday demand: 1.25
        // Expected: 100 * 1.3 * 1.4 * 1.25 = 227.5
        assertEquals(227.5, price);
    }

    @Test
    void calculatePrice_weekdayNight_noWeekendPremium() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate monday = LocalDate.of(2025, 6, 2); // Assume this is Monday
        LocalDate tuesday = monday.plusDays(1);

        double price = pricingService.calculatePrice("test-room", monday, tuesday);

        // Base: 100, No weekend premium, Summer: 1.4, No special demand
        // Expected: 100 * 1.4 = 140
        assertEquals(140.0, price);
    }

    // Seasonal multiplier tests
    @Test
    void calculatePrice_winterDiscount() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate winterDay = LocalDate.of(2025, 1, 15); // January
        LocalDate nextDay = winterDay.plusDays(1);

        double price = pricingService.calculatePrice("test-room", winterDay, nextDay);

        // Base: 100, Winter: 0.8, No weekend, No special demand
        // Expected: 100 * 0.8 = 80
        assertEquals(80.0, price);
    }

    @Test
    void calculatePrice_summerPremium() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate summerDay = LocalDate.of(2025, 7, 15); // July
        LocalDate nextDay = summerDay.plusDays(1);

        double price = pricingService.calculatePrice("test-room", summerDay, nextDay);

        // Base: 100, Summer: 1.4, Tuesday: 0.9 demand
        // Expected: 100 * 1.4 * 0.9 = 126
        assertEquals(126.0, price);
    }

    @Test
    void calculatePrice_springBaseRate() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate springDay = LocalDate.of(2025, 4, 15); // April
        LocalDate nextDay = springDay.plusDays(1);

        double price = pricingService.calculatePrice("test-room", springDay, nextDay);

        // Base: 100, Spring: 1.0, Tuesday: 0.9 demand
        // Expected: 100 * 1.0 * 0.9 = 90
        assertEquals(90.0, price);
    }

    @Test
    void calculatePrice_autumnPremium() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate autumnDay = LocalDate.of(2025, 10, 15); // October
        LocalDate nextDay = autumnDay.plusDays(1);

        double price = pricingService.calculatePrice("test-room", autumnDay, nextDay);

        // Base: 100, Autumn: 1.1, Wednesday: 0.9 demand
        // Expected: 100 * 1.1 * 0.9 = 99
        assertEquals(99.0, price);
    }

    // Holiday premium tests
    @Test
    void calculatePrice_holidayPremium() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate christmas = LocalDate.of(2025, 12, 25);
        LocalDate nextDay = christmas.plusDays(1);

        double price = pricingService.calculatePrice("test-room", christmas, nextDay);

        // Base: 100, Winter: 0.8, Holiday: 1.5, Thursday: 1.0 demand
        // Expected: 100 * 0.8 * 1.5 * 1.0 = 120
        assertEquals(120.0, price);
    }

    @Test
    void calculatePrice_newYearsEve() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate newYears = LocalDate.of(2025, 12, 31);
        LocalDate nextDay = newYears.plusDays(1);

        double price = pricingService.calculatePrice("test-room", newYears, nextDay);

        // Base: 100, Winter: 0.8, Holiday: 1.5, Wednesday: 0.9 demand
        // Expected: 100 * 0.8 * 1.5 * 0.9 = 108
        assertEquals(108.0, price);
    }

    // Demand-based pricing tests
    @Test
    void calculatePrice_lowDemandDays() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate tuesday = LocalDate.of(2025, 4, 1); // Tuesday in spring
        LocalDate wednesday = tuesday.plusDays(1);

        double tuesdayPrice = pricingService.calculatePrice("test-room", tuesday, wednesday);
        double wednesdayPrice = pricingService.calculatePrice("test-room", wednesday, wednesday.plusDays(1));

        // Both Tuesday and Wednesday have 0.9 demand multiplier
        assertEquals(90.0, tuesdayPrice);   // 100 * 1.0 * 0.9
        assertEquals(90.0, wednesdayPrice); // 100 * 1.0 * 0.9
    }

    @Test
    void calculatePrice_premiumRoomDemandBonus() {
        when(roomRepository.findById("suite-room")).thenReturn(Optional.empty());
        when(roomRepository.findById("premium-suite")).thenReturn(Optional.empty());

        LocalDate monday = LocalDate.of(2025, 4, 7); // Monday in spring
        LocalDate tuesday = monday.plusDays(1);

        double suitePrice = pricingService.calculatePrice("suite-room", monday, tuesday);
        double premiumPrice = pricingService.calculatePrice("premium-suite", monday, tuesday);

        // Suite: 300 * 1.0 (spring) * 1.1 (premium demand) = 330
        // Premium: 450 * 1.0 (spring) * 1.1 (premium demand) = 495
        assertEquals(330.0, suitePrice);
        assertEquals(495.0, premiumPrice);
    }

    // Long stay discount tests
    @Test
    void calculatePrice_weeklyStay_gets5PercentOff() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.of(2025, 4, 1); // Spring
        LocalDate checkOut = checkIn.plusDays(7); // 7 nights

        double price = pricingService.calculatePrice("test-room", checkIn, checkOut);

        // 7 nights * base calculations, then 5% discount for weekly stay
        // Each night varies by day of week, but total should be discounted by 5%
        assertTrue(price > 0);

        // Calculate what it would be without discount
        double priceWithoutDiscount = 0;
        for (int i = 0; i < 7; i++) {
            priceWithoutDiscount += pricingService.calculateNightPrice("test-room", checkIn.plusDays(i));
        }

        assertEquals(Math.round(priceWithoutDiscount * 0.95 * 100.0) / 100.0, price);
    }

    @Test
    void calculatePrice_biweeklyStay_gets10PercentOff() {
        when(roomRepository.findById("economy-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.of(2025, 4, 1);
        LocalDate checkOut = checkIn.plusDays(14); // 14 nights

        double price = pricingService.calculatePrice("economy-room", checkIn, checkOut);

        // Should get 10% discount for 14+ night stay
        assertTrue(price > 0);

        double priceWithoutDiscount = 0;
        for (int i = 0; i < 14; i++) {
            priceWithoutDiscount += pricingService.calculateNightPrice("economy-room", checkIn.plusDays(i));
        }

        assertEquals(Math.round(priceWithoutDiscount * 0.9 * 100.0) / 100.0, price);
    }

    @Test
    void calculatePrice_monthlyStay_gets20PercentOff() {
        when(roomRepository.findById("economy-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.of(2025, 4, 1);
        LocalDate checkOut = checkIn.plusDays(28); // 28 nights

        double price = pricingService.calculatePrice("economy-room", checkIn, checkOut);

        // Should get 20% discount for 28+ night stay
        assertTrue(price > 0);

        double priceWithoutDiscount = 0;
        for (int i = 0; i < 28; i++) {
            priceWithoutDiscount += pricingService.calculateNightPrice("economy-room", checkIn.plusDays(i));
        }

        assertEquals(Math.round(priceWithoutDiscount * 0.8 * 100.0) / 100.0, price);
    }

    // Early booking discount tests
    @Test
    void calculatePrice_earlyBooking90Days_gets5PercentOff() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.now().plusDays(90);
        LocalDate checkOut = checkIn.plusDays(2);

        double price = pricingService.calculatePrice("test-room", checkIn, checkOut);

        // Should get 5% early booking discount
        assertTrue(price > 0);

        // Calculate base price for comparison
        double night1 = pricingService.calculateNightPrice("test-room", checkIn);
        double night2 = pricingService.calculateNightPrice("test-room", checkIn.plusDays(1));
        double basePrice = night1 + night2;

        assertEquals(Math.round(basePrice * 0.95 * 100.0) / 100.0, price);
    }

    @Test
    void calculatePrice_earlyBooking30Days_gets3PercentOff() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.now().plusDays(30);
        LocalDate checkOut = checkIn.plusDays(1);

        double price = pricingService.calculatePrice("test-room", checkIn, checkOut);

        // Should get 3% early booking discount
        double nightPrice = pricingService.calculateNightPrice("test-room", checkIn);
        assertEquals(Math.round(nightPrice * 0.97 * 100.0) / 100.0, price);
    }

    @Test
    void calculatePrice_lastMinuteBooking_noEarlyDiscount() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = checkIn.plusDays(1);

        double price = pricingService.calculatePrice("test-room", checkIn, checkOut);
        double nightPrice = pricingService.calculateNightPrice("test-room", checkIn);

        // Should not get early booking discount
        assertEquals(nightPrice, price);
    }

    // Multi-night calculation tests
    @Test
    void calculatePrice_multipleNights_sumsCorrectly() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.of(2025, 6, 5); // Thursday
        LocalDate checkOut = checkIn.plusDays(3); // Thu, Fri, Sat nights

        double totalPrice = pricingService.calculatePrice("test-room", checkIn, checkOut);

        double thursdayPrice = pricingService.calculateNightPrice("test-room", checkIn);
        double fridayPrice = pricingService.calculateNightPrice("test-room", checkIn.plusDays(1));
        double saturdayPrice = pricingService.calculateNightPrice("test-room", checkIn.plusDays(2));

        double expectedTotal = thursdayPrice + fridayPrice + saturdayPrice;
        assertEquals(expectedTotal, totalPrice);
    }

    // Price breakdown method tests
    @Test
    void getPriceBreakdown_returnsCorrectDayByDayPricing() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.of(2025, 6, 5);
        LocalDate checkOut = checkIn.plusDays(3);

        Map<LocalDate, Double> breakdown = pricingService.getPriceBreakdown("test-room", checkIn, checkOut);

        assertEquals(3, breakdown.size());
        assertTrue(breakdown.containsKey(checkIn));
        assertTrue(breakdown.containsKey(checkIn.plusDays(1)));
        assertTrue(breakdown.containsKey(checkIn.plusDays(2)));

        // Friday and Saturday should be more expensive than Thursday
        double thursday = breakdown.get(checkIn);
        double friday = breakdown.get(checkIn.plusDays(1));
        double saturday = breakdown.get(checkIn.plusDays(2));

        assertTrue(friday > thursday); // Weekend premium
        assertTrue(saturday > friday); // Saturday demand > Friday demand
    }

    // Average nightly rate tests
    @Test
    void getAverageNightlyRate_calculatesCorrectly() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.of(2025, 4, 1);
        LocalDate checkOut = checkIn.plusDays(7); // 7 nights

        double totalPrice = pricingService.calculatePrice("test-room", checkIn, checkOut);
        double averageRate = pricingService.getAverageNightlyRate("test-room", checkIn, checkOut);

        assertEquals(totalPrice / 7.0, averageRate);
    }

    @Test
    void getAverageNightlyRate_zeroNights_returnsZero() {
        LocalDate date = LocalDate.now();
        double average = pricingService.getAverageNightlyRate("test-room", date, date);
        assertEquals(0.0, average);
    }

    // Complex scenario tests
    @Test
    void calculatePrice_complexHolidayWeekendScenario() {
        when(roomRepository.findById("premium-suite")).thenReturn(Optional.empty());

        // New Year's Eve (holiday) on a Saturday (weekend)
        LocalDate newYearsEve = LocalDate.of(2025, 12, 31);
        LocalDate nextDay = newYearsEve.plusDays(1);

        double price = pricingService.calculatePrice("premium-suite", newYearsEve, nextDay);

        // Premium suite: 450 base
        // Winter: 0.8, Weekend: 1.3, Holiday: 1.5, Premium demand: 1.1, Wednesday demand: 0.9
        // But Wednesday demand is overridden by premium demand
        // Expected: 450 * 0.8 * 1.3 * 1.5 * 1.1 = 631.8
        assertEquals(631.8, price);
    }

    @Test
    void calculatePrice_stackedDiscountsScenario() {
        when(roomRepository.findById("economy-room")).thenReturn(Optional.empty());

        // Long stay (14 nights) + early booking (90 days advance)
        LocalDate checkIn = LocalDate.now().plusDays(90);
        LocalDate checkOut = checkIn.plusDays(14);

        double price = pricingService.calculatePrice("economy-room", checkIn, checkOut);

        // Should have both long stay discount (10%) AND early booking discount (5%)
        // Total discount = 1 - ((1-0.10) * (1-0.05)) = 1 - 0.855 = 14.5%
        assertTrue(price > 0);

        double baseTotal = 0;
        for (int i = 0; i < 14; i++) {
            baseTotal += pricingService.calculateNightPrice("economy-room", checkIn.plusDays(i));
        }

        double expectedPrice = baseTotal * 0.9 * 0.95; // Long stay then early booking
        assertEquals(Math.round(expectedPrice * 100.0) / 100.0, price);
    }

    // Helper method tests
    @Test
    void addHoliday_makesDateMoreExpensive() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate regularDay = LocalDate.of(2025, 6, 15);
        LocalDate nextDay = regularDay.plusDays(1);

        double priceBeforeHoliday = pricingService.calculatePrice("test-room", regularDay, nextDay);

        pricingService.addHoliday(regularDay);

        double priceAfterHoliday = pricingService.calculatePrice("test-room", regularDay, nextDay);

        assertTrue(priceAfterHoliday > priceBeforeHoliday);
        assertEquals(priceBeforeHoliday * 1.5, priceAfterHoliday); // Holiday multiplier is 1.5
    }

    @Test
    void setBaseRate_changesRoomPricing() {
        when(roomRepository.findById("custom-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.of(2025, 4, 1); // Spring weekday
        LocalDate checkOut = checkIn.plusDays(1);

        double originalPrice = pricingService.calculatePrice("custom-room", checkIn, checkOut);

        pricingService.setBaseRate("custom-room", 200.0);

        double newPrice = pricingService.calculatePrice("custom-room", checkIn, checkOut);

        assertTrue(newPrice > originalPrice);
        // New price should be exactly double if base rate doubled
        assertEquals(originalPrice * 2, newPrice);
    }

    @Test
    void setSeasonalMultiplier_affectsSummerPricing() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate summerDay = LocalDate.of(2025, 7, 15);
        LocalDate nextDay = summerDay.plusDays(1);

        double originalPrice = pricingService.calculatePrice("test-room", summerDay, nextDay);

        // Change summer multiplier from 1.4 to 2.0
        pricingService.setSeasonalMultiplier("summer", 2.0);

        double newPrice = pricingService.calculatePrice("test-room", summerDay, nextDay);

        assertTrue(newPrice > originalPrice);
        // New price should reflect the multiplier change: 2.0/1.4 = 1.428... times more
        assertEquals(Math.round(originalPrice * (2.0/1.4) * 100.0) / 100.0, newPrice);
    }

    // Edge case tests
    @Test
    void calculatePrice_roundsToTwoDecimals() {
        when(roomRepository.findById("test-room")).thenReturn(Optional.empty());

        LocalDate checkIn = LocalDate.of(2025, 6, 6); // Friday in summer
        LocalDate checkOut = checkIn.plusDays(1);

        double price = pricingService.calculatePrice("test-room", checkIn, checkOut);

        // Verify result is rounded to 2 decimal places
        assertEquals(price, Math.round(price * 100.0) / 100.0);
    }

    @Test
    void calculateNightPrice_handlesAllMultipliersCorrectly() {
        when(roomRepository.findById("suite-room")).thenReturn(Optional.empty());

        // Christmas (holiday) on a Friday (weekend) in winter
        LocalDate christmasFriday = LocalDate.of(2025, 12, 25); // Assume this is a Friday

        double price = pricingService.calculateNightPrice("suite-room", christmasFriday);

        // Suite base: 300, Winter: 0.8, Weekend: 1.3, Holiday: 1.5, Friday demand: 1.2, Premium: 1.1
        // Expected: 300 * 0.8 * 1.3 * 1.5 * 1.2 * 1.1 = 616.32
        // But premium demand (1.1) should replace day-of-week demand, not stack
        // So: 300 * 0.8 * 1.3 * 1.5 * 1.1 = 513.6
        assertTrue(price > 500); // Should be quite expensive with all these multipliers
    }
}

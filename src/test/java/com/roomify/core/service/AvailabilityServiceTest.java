package com.roomify.core.service;

import com.roomify.core.dto.Booking;
import com.roomify.core.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnhancedAvailabilityServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        availabilityService = new AvailabilityService(bookingRepository);
    }

    // Basic validation tests
    @Test
    void isAvailable_nullParameters_returnsFalse() {
        assertFalse(availabilityService.isAvailable(null, LocalDate.now(), LocalDate.now().plusDays(1)));
        assertFalse(availabilityService.isAvailable("room1", null, LocalDate.now().plusDays(1)));
        assertFalse(availabilityService.isAvailable("room1", LocalDate.now(), null));
    }

    @Test
    void isAvailable_invalidDateRange_returnsFalse() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // From date after to date
        assertFalse(availabilityService.isAvailable("room1", today.plusDays(2), today.plusDays(1)));

        // Same from and to date
        assertFalse(availabilityService.isAvailable("room1", today, today));
    }

    @Test
    void isAvailable_pastDate_returnsFalse() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();

        assertFalse(availabilityService.isAvailable("room1", yesterday, today));
    }

    // Maintenance room tests
    @Test
    void isAvailable_maintenanceRoom_returnsFalse() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfter = LocalDate.now().plusDays(2);

        assertFalse(availabilityService.isAvailable("room-maintenance-1", tomorrow, dayAfter));
        assertFalse(availabilityService.isAvailable("room-maintenance-2", tomorrow, dayAfter));
    }

    @Test
    void isAvailable_regularRoom_notInMaintenance() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfter = LocalDate.now().plusDays(2);

        when(bookingRepository.findByRoomId("regular-room")).thenReturn(Collections.emptyList());

        assertTrue(availabilityService.isAvailable("regular-room", tomorrow, dayAfter));
    }

    // Blocked dates tests
    @Test
    void isAvailable_blockedDate_returnsFalse() {
        LocalDate christmas = LocalDate.of(2025, 12, 25);
        LocalDate dayAfter = christmas.plusDays(1);

        when(bookingRepository.findByRoomId("room-1")).thenReturn(Collections.emptyList());

        assertFalse(availabilityService.isAvailable("room-1", christmas, dayAfter));
    }

    @Test
    void isAvailable_rangeIncludesBlockedDate_returnsFalse() {
        LocalDate before = LocalDate.of(2025, 12, 24);
        LocalDate after = LocalDate.of(2025, 12, 26);

        when(bookingRepository.findByRoomId("room-1")).thenReturn(Collections.emptyList());

        // Range includes Christmas Day (blocked)
        assertFalse(availabilityService.isAvailable("room-1", before, after));
    }

    @Test
    void isAvailable_noBlockedDatesInRange_returnsTrue() {
        LocalDate normalDay = LocalDate.of(2025, 6, 1);
        LocalDate nextDay = normalDay.plusDays(1);

        when(bookingRepository.findByRoomId("room-1")).thenReturn(Collections.emptyList());

        assertTrue(availabilityService.isAvailable("room-1", normalDay, nextDay));
    }

    // Existing booking overlap tests
    @Test
    void isAvailable_overlappingBooking_returnsFalse() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(7);

        // Existing booking from day 4 to day 6 (overlaps with our 5-7 request)
        Booking existingBooking = new Booking();
        existingBooking.setRoomId("room1");
        existingBooking.setFrom(LocalDate.now().plusDays(4));
        existingBooking.setTo(LocalDate.now().plusDays(6));

        when(bookingRepository.findByRoomId("room1")).thenReturn(List.of(existingBooking));

        assertFalse(availabilityService.isAvailable("room1", checkIn, checkOut));
    }

    @Test
    void isAvailable_noOverlappingBookings_returnsTrue() {
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = LocalDate.now().plusDays(12);

        // Existing booking that doesn't overlap (days 5-7)
        Booking existingBooking = new Booking();
        existingBooking.setRoomId("room1");
        existingBooking.setFrom(LocalDate.now().plusDays(5));
        existingBooking.setTo(LocalDate.now().plusDays(7));

        when(bookingRepository.findByRoomId("room1")).thenReturn(List.of(existingBooking));

        assertTrue(availabilityService.isAvailable("room1", checkIn, checkOut));
    }

    @Test
    void isAvailable_adjacentBookings_returnsTrue() {
        LocalDate checkIn = LocalDate.now().plusDays(7);
        LocalDate checkOut = LocalDate.now().plusDays(9);

        // Existing booking ends exactly when new one starts (days 5-7)
        Booking existingBooking = new Booking();
        existingBooking.setRoomId("room1");
        existingBooking.setFrom(LocalDate.now().plusDays(5));
        existingBooking.setTo(LocalDate.now().plusDays(7));

        when(bookingRepository.findByRoomId("room1")).thenReturn(List.of(existingBooking));

        assertTrue(availabilityService.isAvailable("room1", checkIn, checkOut));
    }

    // Business rules tests
    @Test
    void isAvailable_maximumStayExceeded_returnsFalse() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(31); // 31 days > 30 day limit

        when(bookingRepository.findByRoomId("room1")).thenReturn(Collections.emptyList());

        assertFalse(availabilityService.isAvailable("room1", checkIn, checkOut));
    }

    @Test
    void isAvailable_exactly30Days_returnsTrue() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(30); // Exactly 30 days

        when(bookingRepository.findByRoomId("room1")).thenReturn(Collections.emptyList());

        assertTrue(availabilityService.isAvailable("room1", checkIn, checkOut));
    }

    @Test
    void isAvailable_tooFarInAdvance_returnsFalse() {
        LocalDate checkIn = LocalDate.now().plusDays(366); // More than 365 days
        LocalDate checkOut = checkIn.plusDays(1);

        when(bookingRepository.findByRoomId("room1")).thenReturn(Collections.emptyList());

        assertFalse(availabilityService.isAvailable("room1", checkIn, checkOut));
    }

    @Test
    void isAvailable_exactly365DaysInAdvance_returnsTrue() {
        LocalDate checkIn = LocalDate.now().plusDays(365); // Exactly 365 days
        LocalDate checkOut = checkIn.plusDays(1);

        when(bookingRepository.findByRoomId("room1")).thenReturn(Collections.emptyList());

        assertTrue(availabilityService.isAvailable("room1", checkIn, checkOut));
    }

    @Test
    void isAvailable_weekendSuiteOneNight_returnsFalse() {
        // Find next Saturday
        LocalDate saturday = findNextSaturday();
        LocalDate sunday = saturday.plusDays(1);

        when(bookingRepository.findByRoomId("suite-room")).thenReturn(Collections.emptyList());

        // Suite rooms require minimum 2 nights on weekends
        assertFalse(availabilityService.isAvailable("suite-room", saturday, sunday));
    }

    @Test
    void isAvailable_weekendSuiteTwoNights_returnsTrue() {
        LocalDate saturday = findNextSaturday();
        LocalDate monday = saturday.plusDays(2); // 2 nights

        when(bookingRepository.findByRoomId("suite-room")).thenReturn(Collections.emptyList());

        assertTrue(availabilityService.isAvailable("suite-room", saturday, monday));
    }

    @Test
    void isAvailable_weekdaySuiteOneNight_returnsTrue() {
        LocalDate monday = findNextMonday();
        LocalDate tuesday = monday.plusDays(1);

        when(bookingRepository.findByRoomId("suite-room")).thenReturn(Collections.emptyList());

        // Weekend minimum doesn't apply to weekdays
        assertTrue(availabilityService.isAvailable("suite-room", monday, tuesday));
    }

    // getAvailableDates method tests
    @Test
    void getAvailableDates_nullParameters_returnsEmptyList() {
        List<LocalDate> result = availabilityService.getAvailableDates(null, LocalDate.now(), LocalDate.now().plusDays(3));
        assertTrue(result.isEmpty());

        result = availabilityService.getAvailableDates("room1", null, LocalDate.now().plusDays(3));
        assertTrue(result.isEmpty());

        result = availabilityService.getAvailableDates("room1", LocalDate.now(), null);
        assertTrue(result.isEmpty());
    }

    @Test
    void getAvailableDates_mixedAvailability_returnsOnlyAvailableDates() {
        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = start.plusDays(5);

        // Mock existing booking that blocks some dates
        Booking existingBooking = new Booking();
        existingBooking.setRoomId("room1");
        existingBooking.setFrom(start.plusDays(2));
        existingBooking.setTo(start.plusDays(4));

        when(bookingRepository.findByRoomId("room1")).thenReturn(List.of(existingBooking));

        List<LocalDate> availableDates = availabilityService.getAvailableDates("room1", start, end);

        // Should have days 1, 2, and 5 available (days 3 and 4 are booked)
        assertEquals(3, availableDates.size());
        assertTrue(availableDates.contains(start));
        assertTrue(availableDates.contains(start.plusDays(1)));
        assertTrue(availableDates.contains(start.plusDays(4)));
    }

    // checkMultipleRooms method tests
    @Test
    void checkMultipleRooms_mixedAvailability_returnsCorrectMap() {
        List<String> roomIds = Arrays.asList("available-room", "room-maintenance-1", "booked-room");
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(2);

        // Set up mock for booked room
        Booking existingBooking = new Booking();
        existingBooking.setRoomId("booked-room");
        existingBooking.setFrom(checkIn);
        existingBooking.setTo(checkOut);

        when(bookingRepository.findByRoomId("available-room")).thenReturn(Collections.emptyList());
        when(bookingRepository.findByRoomId("room-maintenance-1")).thenReturn(Collections.emptyList());
        when(bookingRepository.findByRoomId("booked-room")).thenReturn(List.of(existingBooking));

        Map<String, Boolean> result = availabilityService.checkMultipleRooms(roomIds, checkIn, checkOut);

        assertEquals(3, result.size());
        assertTrue(result.get("available-room"));    // Available
        assertFalse(result.get("room-maintenance-1")); // Under maintenance
        assertFalse(result.get("booked-room"));       // Already booked
    }

    // Helper method tests (testing the test helpers)
    @Test
    void addMaintenanceRoom_makesRoomUnavailable() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(2);

        when(bookingRepository.findByRoomId("test-room")).thenReturn(Collections.emptyList());

        // Initially available
        assertTrue(availabilityService.isAvailable("test-room", checkIn, checkOut));

        // Add to maintenance
        availabilityService.addMaintenanceRoom("test-room");

        // Now unavailable
        assertFalse(availabilityService.isAvailable("test-room", checkIn, checkOut));
    }

    @Test
    void removeMaintenanceRoom_makesRoomAvailable() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(2);

        when(bookingRepository.findByRoomId("room-maintenance-1")).thenReturn(Collections.emptyList());

        // Initially under maintenance
        assertFalse(availabilityService.isAvailable("room-maintenance-1", checkIn, checkOut));

        // Remove from maintenance
        availabilityService.removeMaintenanceRoom("room-maintenance-1");

        // Now available
        assertTrue(availabilityService.isAvailable("room-maintenance-1", checkIn, checkOut));
    }

    @Test
    void blockDate_makesDateUnavailable() {
        LocalDate blockDate = LocalDate.now().plusDays(10);
        LocalDate nextDay = blockDate.plusDays(1);

        when(bookingRepository.findByRoomId("test-room")).thenReturn(Collections.emptyList());

        // Initially available
        assertTrue(availabilityService.isAvailable("test-room", blockDate, nextDay));

        // Block the date
        availabilityService.blockDate("test-room", blockDate);

        // Now unavailable
        assertFalse(availabilityService.isAvailable("test-room", blockDate, nextDay));
    }

    @Test
    void unblockDate_makesDateAvailable() {
        LocalDate christmas = LocalDate.of(2025, 12, 25);
        LocalDate dayAfter = christmas.plusDays(1);

        when(bookingRepository.findByRoomId("room-1")).thenReturn(Collections.emptyList());

        // Initially blocked (Christmas is pre-blocked for room-1)
        assertFalse(availabilityService.isAvailable("room-1", christmas, dayAfter));

        // Unblock Christmas
        availabilityService.unblockDate("room-1", christmas);

        // Now available
        assertTrue(availabilityService.isAvailable("room-1", christmas, dayAfter));
    }

    // Complex scenario tests
    @Test
    void isAvailable_complexScenarioWithMultipleRestrictions() {
        // Scenario: Premium suite, weekend booking, with existing booking nearby
        LocalDate saturday = findNextSaturday();
        LocalDate monday = saturday.plusDays(2); // 2 nights (meets weekend minimum)

        // Existing booking that doesn't overlap
        Booking existingBooking = new Booking();
        existingBooking.setRoomId("premium-suite");
        existingBooking.setFrom(saturday.minusDays(3));
        existingBooking.setTo(saturday.minusDays(1));

        when(bookingRepository.findByRoomId("premium-suite")).thenReturn(List.of(existingBooking));

        assertTrue(availabilityService.isAvailable("premium-suite", saturday, monday));
    }

    @Test
    void isAvailable_edgeCaseExactOverlap() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(7);

        // Existing booking with exact same dates
        Booking exactOverlap = new Booking();
        exactOverlap.setRoomId("room1");
        exactOverlap.setFrom(checkIn);
        exactOverlap.setTo(checkOut);

        when(bookingRepository.findByRoomId("room1")).thenReturn(List.of(exactOverlap));

        assertFalse(availabilityService.isAvailable("room1", checkIn, checkOut));
    }

    // Helper methods for tests
    private LocalDate findNextSaturday() {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek().getValue() != 6) { // 6 = Saturday
            date = date.plusDays(1);
        }
        return date;
    }

    private LocalDate findNextMonday() {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek().getValue() != 1) { // 1 = Monday
            date = date.plusDays(1);
        }
        return date;
    }
}
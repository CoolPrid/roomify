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
class AvailabilityServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    private AvailabilityService availabilityService;

    @BeforeEach
    void setUp() {
        availabilityService = new AvailabilityService(bookingRepository);
    }

    @Test
    void isAvailable_nullParameters_returnsFalse() {
        assertFalse(availabilityService.isAvailable(null, LocalDate.now(), LocalDate.now().plusDays(1)));
        assertFalse(availabilityService.isAvailable("room1", null, LocalDate.now().plusDays(1)));
        assertFalse(availabilityService.isAvailable("room1", LocalDate.now(), null));
    }

    @Test
    void isAvailable_invalidDateRange_returnsFalse() {
        LocalDate today = LocalDate.now();

        assertFalse(availabilityService.isAvailable("room1", today.plusDays(2), today.plusDays(1)));
        assertFalse(availabilityService.isAvailable("room1", today, today));
    }

    @Test
    void isAvailable_maintenanceRoom_returnsFalse() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalDate dayAfter = LocalDate.now().plusDays(2);

        assertFalse(availabilityService.isAvailable("room-maintenance-1", tomorrow, dayAfter));
    }

    @Test
    void isAvailable_blockedDate_returnsFalse() {
        LocalDate christmas = LocalDate.of(2025, 12, 25);
        LocalDate dayAfter = christmas.plusDays(1);

        // No need to stub for blocked dates test since it's handled internally
        assertFalse(availabilityService.isAvailable("room-1", christmas, dayAfter));
    }

    @Test
    void isAvailable_overlappingBooking_returnsFalse() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(7);

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

        Booking existingBooking = new Booking();
        existingBooking.setRoomId("room1");
        existingBooking.setFrom(LocalDate.now().plusDays(5));
        existingBooking.setTo(LocalDate.now().plusDays(7));

        when(bookingRepository.findByRoomId("room1")).thenReturn(List.of(existingBooking));

        assertTrue(availabilityService.isAvailable("room1", checkIn, checkOut));
    }

    @Test
    void isAvailable_maximumStayExceeded_returnsFalse() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(31); // 31 days > 30 day limit

        when(bookingRepository.findByRoomId("room1")).thenReturn(Collections.emptyList());

        assertFalse(availabilityService.isAvailable("room1", checkIn, checkOut));
    }

    @Test
    void isAvailable_weekendSuiteOneNight_returnsFalse() {
        LocalDate saturday = findNextSaturday();
        LocalDate sunday = saturday.plusDays(1);

        when(bookingRepository.findByRoomId("suite-room")).thenReturn(Collections.emptyList());

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
    void checkMultipleRooms_mixedAvailability_returnsCorrectMap() {
        List<String> roomIds = Arrays.asList("available-room", "room-maintenance-1", "booked-room");
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(2);

        Booking existingBooking = new Booking();
        existingBooking.setRoomId("booked-room");
        existingBooking.setFrom(checkIn);
        existingBooking.setTo(checkOut);

        when(bookingRepository.findByRoomId("available-room")).thenReturn(Collections.emptyList());
        when(bookingRepository.findByRoomId("booked-room")).thenReturn(List.of(existingBooking));

        Map<String, Boolean> result = availabilityService.checkMultipleRooms(roomIds, checkIn, checkOut);

        assertEquals(3, result.size());
        assertTrue(result.get("available-room"));
        assertFalse(result.get("room-maintenance-1")); // Under maintenance
        assertFalse(result.get("booked-room")); // Already booked
    }

    private LocalDate findNextSaturday() {
        LocalDate date = LocalDate.now().plusDays(1);
        while (date.getDayOfWeek().getValue() != 6) { // 6 = Saturday
            date = date.plusDays(1);
        }
        return date;
    }
}
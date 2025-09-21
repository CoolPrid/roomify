package com.roomify.core.service;

import com.roomify.core.dto.Booking;
import com.roomify.core.repository.BookingRepository;
import java.time.LocalDate;
import java.util.*;

public class AvailabilityService {

    private final BookingRepository bookingRepository;
    private final Set<String> maintenanceRooms;
    private final Map<String, Set<LocalDate>> blockedDates;

    public AvailabilityService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
        this.maintenanceRooms = new HashSet<>();
        this.blockedDates = new HashMap<>();

        // Some rooms are under maintenance
        maintenanceRooms.add("room-maintenance-1");
        maintenanceRooms.add("room-maintenance-2");

        // Some rooms have blocked dates (owner use, repairs, etc.)
        Set<LocalDate> room1Blocked = new HashSet<>();
        room1Blocked.add(LocalDate.of(2025, 12, 24)); // Christmas Eve
        room1Blocked.add(LocalDate.of(2025, 12, 25)); // Christmas Day
        room1Blocked.add(LocalDate.of(2025, 12, 31)); // New Year's Eve
        blockedDates.put("room-1", room1Blocked);

        Set<LocalDate> room2Blocked = new HashSet<>();
        room2Blocked.add(LocalDate.of(2025, 7, 4));   // Independence Day
        room2Blocked.add(LocalDate.of(2025, 11, 28)); // Thanksgiving
        blockedDates.put("room-2", room2Blocked);
    }

    public boolean isAvailable(String roomId, LocalDate from, LocalDate to) {
        // Validation checks
        if (roomId == null || from == null || to == null) {
            return false;
        }

        if (from.isAfter(to) || from.equals(to)) {
            return false; // Invalid date range
        }

        if (from.isBefore(LocalDate.now())) {
            return false; // Can't book in the past
        }

        // Check if room is under maintenance
        if (isRoomUnderMaintenance(roomId)) {
            return false;
        }

        // Check if any dates in the range are blocked
        if (hasBlockedDatesInRange(roomId, from, to)) {
            return false;
        }

        // Check for existing bookings that overlap
        if (hasOverlappingBookings(roomId, from, to)) {
            return false;
        }

        // Check business rules
        if (!meetBusinessRules(roomId, from, to)) {
            return false;
        }

        return true;
    }

    public List<LocalDate> getAvailableDates(String roomId, LocalDate from, LocalDate to) {
        List<LocalDate> availableDates = new ArrayList<>();

        if (roomId == null || from == null || to == null) {
            return availableDates;
        }

        LocalDate current = from;
        while (!current.isAfter(to)) {
            LocalDate next = current.plusDays(1);
            if (isAvailable(roomId, current, next)) {
                availableDates.add(current);
            }
            current = next;
        }

        return availableDates;
    }

    public Map<String, Boolean> checkMultipleRooms(List<String> roomIds, LocalDate from, LocalDate to) {
        Map<String, Boolean> availability = new HashMap<>();

        for (String roomId : roomIds) {
            availability.put(roomId, isAvailable(roomId, from, to));
        }

        return availability;
    }

    private boolean isRoomUnderMaintenance(String roomId) {
        return maintenanceRooms.contains(roomId);
    }

    private boolean hasBlockedDatesInRange(String roomId, LocalDate from, LocalDate to) {
        Set<LocalDate> blocked = blockedDates.get(roomId);
        if (blocked == null || blocked.isEmpty()) {
            return false;
        }

        LocalDate current = from;
        while (current.isBefore(to)) {
            if (blocked.contains(current)) {
                return true;
            }
            current = current.plusDays(1);
        }

        return false;
    }

    private boolean hasOverlappingBookings(String roomId, LocalDate from, LocalDate to) {
        if (bookingRepository == null) {
            return false; // No bookings to check against
        }

        List<Booking> existingBookings = bookingRepository.findByRoomId(roomId);

        for (Booking booking : existingBookings) {
            if (datesOverlap(from, to, booking.getFrom(), booking.getTo())) {
                return true;
            }
        }

        return false;
    }

    private boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        // Two date ranges overlap if: start1 < end2 AND start2 < end1
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private boolean meetBusinessRules(String roomId, LocalDate from, LocalDate to) {
        // Business Rule 1: Maximum 30-day booking
        long days = java.time.temporal.ChronoUnit.DAYS.between(from, to);
        if (days > 30) {
            return false;
        }

        // Business Rule 2: Minimum 1-night stay
        if (days < 1) {
            return false;
        }

        // Business Rule 3: Can't book more than 365 days in advance
        long daysInAdvance = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), from);
        if (daysInAdvance > 365) {
            return false;
        }

        // Business Rule 4: Some rooms require minimum 2-night stays on weekends
        if (isWeekendCheckIn(from) && isShortStayRoom(roomId) && days < 2) {
            return false;
        }

        return true;
    }

    private boolean isWeekendCheckIn(LocalDate checkIn) {
        int dayOfWeek = checkIn.getDayOfWeek().getValue();
        return dayOfWeek >= 6; // Saturday=6, Sunday=7
    }

    private boolean isShortStayRoom(String roomId) {
        // Some rooms have minimum stay requirements
        return roomId.contains("suite") || roomId.contains("premium");
    }

    // Helper methods for testing
    public void addMaintenanceRoom(String roomId) {
        maintenanceRooms.add(roomId);
    }

    public void removeMaintenanceRoom(String roomId) {
        maintenanceRooms.remove(roomId);
    }

    public void blockDate(String roomId, LocalDate date) {
        blockedDates.computeIfAbsent(roomId, k -> new HashSet<>()).add(date);
    }

    public void unblockDate(String roomId, LocalDate date) {
        Set<LocalDate> blocked = blockedDates.get(roomId);
        if (blocked != null) {
            blocked.remove(date);
        }
    }
}

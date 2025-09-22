package com.roomify.core.service;

import com.roomify.core.dto.Booking;
import com.roomify.core.repository.BookingRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class AvailabilityService {

    private static final int MAX_BOOKING_DAYS = 30;
    private static final int MIN_BOOKING_DAYS = 1;
    private static final int MAX_ADVANCE_BOOKING_DAYS = 365;
    private static final int WEEKEND_MIN_NIGHTS_PREMIUM_ROOMS = 2;

    private final BookingRepository bookingRepository;
    private final Set<String> maintenanceRooms;
    private final Map<String, Set<LocalDate>> blockedDates;

    public AvailabilityService() {
        this(null);
    }

    public AvailabilityService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
        this.maintenanceRooms = new HashSet<>();
        this.blockedDates = new HashMap<>();

        if (bookingRepository != null) {
            initializeMaintenanceRooms();
            initializeBlockedDates();
        }
    }

    public boolean isAvailable(String roomId, LocalDate from, LocalDate to) {
        if (bookingRepository == null) {
            return true; // Backward compatibility mode
        }

        if (!isValidRequest(roomId, from, to)) {
            return false;
        }

        return !isRoomUnderMaintenance(roomId) &&
                !hasBlockedDatesInRange(roomId, from, to) &&
                !hasOverlappingBookings(roomId, from, to) &&
                meetBusinessRules(roomId, from, to);
    }

    public List<LocalDate> getAvailableDates(String roomId, LocalDate from, LocalDate to) {
        List<LocalDate> availableDates = new ArrayList<>();

        if (roomId == null || from == null || to == null) {
            return availableDates;
        }

        LocalDate current = from;
        while (!current.isAfter(to)) {
            if (isAvailable(roomId, current, current.plusDays(1))) {
                availableDates.add(current);
            }
            current = current.plusDays(1);
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

    private void initializeMaintenanceRooms() {
        maintenanceRooms.add("room-maintenance-1");
        maintenanceRooms.add("room-maintenance-2");
    }

    private void initializeBlockedDates() {
        Set<LocalDate> room1Blocked = new HashSet<>();
        room1Blocked.add(LocalDate.of(2025, 12, 24));
        room1Blocked.add(LocalDate.of(2025, 12, 25));
        room1Blocked.add(LocalDate.of(2025, 12, 31));
        blockedDates.put("room-1", room1Blocked);

        Set<LocalDate> room2Blocked = new HashSet<>();
        room2Blocked.add(LocalDate.of(2025, 7, 4));
        room2Blocked.add(LocalDate.of(2025, 11, 28));
        blockedDates.put("room-2", room2Blocked);
    }

    private boolean isValidRequest(String roomId, LocalDate from, LocalDate to) {
        return roomId != null && from != null && to != null &&
                from.isBefore(to) &&
                !from.isBefore(LocalDate.now());
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
            return false;
        }

        List<Booking> existingBookings = bookingRepository.findByRoomId(roomId);

        return existingBookings.stream()
                .anyMatch(booking -> datesOverlap(from, to, booking.getFrom(), booking.getTo()));
    }

    private boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private boolean meetBusinessRules(String roomId, LocalDate from, LocalDate to) {
        long stayDuration = ChronoUnit.DAYS.between(from, to);
        long daysInAdvance = ChronoUnit.DAYS.between(LocalDate.now(), from);

        if (stayDuration > MAX_BOOKING_DAYS || stayDuration < MIN_BOOKING_DAYS) {
            return false;
        }

        if (daysInAdvance > MAX_ADVANCE_BOOKING_DAYS) {
            return false;
        }

        if (isWeekendCheckIn(from) && isPremiumRoom(roomId) && stayDuration < WEEKEND_MIN_NIGHTS_PREMIUM_ROOMS) {
            return false;
        }

        return true;
    }

    private boolean isWeekendCheckIn(LocalDate checkIn) {
        return checkIn.getDayOfWeek().getValue() >= 6;
    }

    private boolean isPremiumRoom(String roomId) {
        return roomId.contains("suite") || roomId.contains("premium");
    }

    // Management methods for testing and administration
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

package com.roomify.core.service;

import com.roomify.core.dto.Room;
import com.roomify.core.repository.RoomRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class PricingService {

    private final RoomRepository roomRepository;
    private final Map<String, Double> baseRates;
    private final Map<String, Double> seasonalMultipliers;
    private final Set<LocalDate> holidayDates;

    public PricingService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
        this.baseRates = initializeBaseRates();
        this.seasonalMultipliers = initializeSeasonalMultipliers();
        this.holidayDates = initializeHolidayDates();
    }

    public double calculatePrice(String roomId, LocalDate from, LocalDate to) {
        if (roomId == null || from == null || to == null) {
            return 0.0;
        }

        if (from.isAfter(to) || from.equals(to)) {
            return 0.0;
        }

        long nights = ChronoUnit.DAYS.between(from, to);
        if (nights <= 0) {
            return 0.0;
        }

        double totalPrice = 0.0;
        LocalDate currentDate = from;

        // Calculate price for each night
        for (int i = 0; i < nights; i++) {
            double nightPrice = calculateNightPrice(roomId, currentDate);
            totalPrice += nightPrice;
            currentDate = currentDate.plusDays(1);
        }

        // Apply long stay discounts
        totalPrice = applyLongStayDiscount(totalPrice, nights);

        // Apply early booking discounts
        totalPrice = applyEarlyBookingDiscount(totalPrice, from);

        return Math.round(totalPrice * 100.0) / 100.0;
    }

    public double calculateNightPrice(String roomId, LocalDate date) {
        double baseRate = getBaseRate(roomId);

        // Apply weekend premium
        if (isWeekend(date)) {
            baseRate *= 1.3; // 30% weekend premium
        }

        // Apply seasonal multiplier
        baseRate *= getSeasonalMultiplier(date);

        // Apply holiday premium
        if (isHoliday(date)) {
            baseRate *= 1.5; // 50% holiday premium
        }

        // Apply demand-based pricing (simulated)
        baseRate *= getDemandMultiplier(roomId, date);

        return baseRate;
    }

    public Map<LocalDate, Double> getPriceBreakdown(String roomId, LocalDate from, LocalDate to) {
        Map<LocalDate, Double> breakdown = new LinkedHashMap<>();

        LocalDate currentDate = from;
        while (currentDate.isBefore(to)) {
            double nightPrice = calculateNightPrice(roomId, currentDate);
            breakdown.put(currentDate, nightPrice);
            currentDate = currentDate.plusDays(1);
        }

        return breakdown;
    }

    public double getAverageNightlyRate(String roomId, LocalDate from, LocalDate to) {
        double totalPrice = calculatePrice(roomId, from, to);
        long nights = ChronoUnit.DAYS.between(from, to);

        if (nights <= 0) {
            return 0.0;
        }

        return totalPrice / nights;
    }

    private double getBaseRate(String roomId) {
        // First try to get from room repository
        if (roomRepository != null) {
            Optional<Room> room = roomRepository.findById(roomId);
            if (room.isPresent()) {
                return room.get().getBasePrice();
            }
        }

        // Fallback to predefined rates
        return baseRates.getOrDefault(roomId, 100.0);
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.FRIDAY || dayOfWeek == DayOfWeek.SATURDAY;
    }

    private double getSeasonalMultiplier(LocalDate date) {
        String season = getSeason(date);
        return seasonalMultipliers.getOrDefault(season, 1.0);
    }

    private String getSeason(LocalDate date) {
        int month = date.getMonthValue();

        if (month >= 12 || month <= 2) {
            return "winter";
        } else if (month >= 3 && month <= 5) {
            return "spring";
        } else if (month >= 6 && month <= 8) {
            return "summer";
        } else {
            return "autumn";
        }
    }

    private boolean isHoliday(LocalDate date) {
        return holidayDates.contains(date);
    }

    private double getDemandMultiplier(String roomId, LocalDate date) {
        // Simulate demand-based pricing
        // In reality, this would check booking patterns, events, etc.

        // Higher demand on Fridays and Saturdays
        if (date.getDayOfWeek() == DayOfWeek.FRIDAY) {
            return 1.2;
        }
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return 1.25;
        }

        // Lower demand on Tuesdays and Wednesdays
        if (date.getDayOfWeek() == DayOfWeek.TUESDAY ||
                date.getDayOfWeek() == DayOfWeek.WEDNESDAY) {
            return 0.9;
        }

        // Premium rooms have higher base demand
        if (roomId.contains("suite") || roomId.contains("premium")) {
            return 1.1;
        }

        return 1.0;
    }

    private double applyLongStayDiscount(double totalPrice, long nights) {
        if (nights >= 28) {
            return totalPrice * 0.8; // 20% discount for monthly stays
        } else if (nights >= 14) {
            return totalPrice * 0.9; // 10% discount for bi-weekly stays
        } else if (nights >= 7) {
            return totalPrice * 0.95; // 5% discount for weekly stays
        }
        return totalPrice;
    }

    private double applyEarlyBookingDiscount(double totalPrice, LocalDate checkIn) {
        long daysInAdvance = ChronoUnit.DAYS.between(LocalDate.now(), checkIn);

        if (daysInAdvance >= 90) {
            return totalPrice * 0.95; // 5% discount for 90+ days advance
        } else if (daysInAdvance >= 30) {
            return totalPrice * 0.97; // 3% discount for 30+ days advance
        }

        return totalPrice;
    }

    private Map<String, Double> initializeBaseRates() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("economy-room", 80.0);
        rates.put("standard-room", 120.0);
        rates.put("deluxe-room", 180.0);
        rates.put("suite-room", 300.0);
        rates.put("premium-suite", 450.0);
        rates.put("penthouse", 800.0);
        return rates;
    }

    private Map<String, Double> initializeSeasonalMultipliers() {
        Map<String, Double> multipliers = new HashMap<>();
        multipliers.put("winter", 0.8);  // 20% discount in winter
        multipliers.put("spring", 1.0);  // Base rate in spring
        multipliers.put("summer", 1.4);  // 40% premium in summer
        multipliers.put("autumn", 1.1);  // 10% premium in autumn
        return multipliers;
    }

    private Set<LocalDate> initializeHolidayDates() {
        Set<LocalDate> holidays = new HashSet<>();

        // 2025 holidays
        holidays.add(LocalDate.of(2025, 1, 1));   // New Year's Day
        holidays.add(LocalDate.of(2025, 7, 4));   // Independence Day
        holidays.add(LocalDate.of(2025, 12, 25)); // Christmas Day
        holidays.add(LocalDate.of(2025, 12, 31)); // New Year's Eve
        holidays.add(LocalDate.of(2025, 11, 27)); // Thanksgiving
        holidays.add(LocalDate.of(2025, 2, 14));  // Valentine's Day

        return holidays;
    }

    // Helper methods for testing
    public void addHoliday(LocalDate date) {
        holidayDates.add(date);
    }

    public void removeHoliday(LocalDate date) {
        holidayDates.remove(date);
    }

    public void setBaseRate(String roomId, double rate) {
        baseRates.put(roomId, rate);
    }

    public void setSeasonalMultiplier(String season, double multiplier) {
        seasonalMultipliers.put(season, multiplier);
    }
}

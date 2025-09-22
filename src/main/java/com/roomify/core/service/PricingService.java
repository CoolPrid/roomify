package com.roomify.core.service;

import com.roomify.core.dto.Room;
import com.roomify.core.repository.RoomRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;


public class PricingService {

    private static final double WEEKEND_PREMIUM = 1.3;
    private static final double HOLIDAY_PREMIUM = 1.5;
    private static final double LONG_STAY_WEEKLY_DISCOUNT = 0.95;
    private static final double LONG_STAY_BIWEEKLY_DISCOUNT = 0.9;
    private static final double LONG_STAY_MONTHLY_DISCOUNT = 0.8;
    private static final double EARLY_BOOKING_30_DAYS_DISCOUNT = 0.97;
    private static final double EARLY_BOOKING_90_DAYS_DISCOUNT = 0.95;

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
        if (!isValidPricingRequest(roomId, from, to)) {
            return 0.0;
        }

        long nights = ChronoUnit.DAYS.between(from, to);
        double totalPrice = calculateNightlyRates(roomId, from, to);

        totalPrice = applyLongStayDiscount(totalPrice, nights);
        totalPrice = applyEarlyBookingDiscount(totalPrice, from);

        return Math.round(totalPrice * 100.0) / 100.0;
    }

    public double calculateNightPrice(String roomId, LocalDate date) {
        double price = getBaseRate(roomId);

        if (isWeekend(date)) {
            price *= WEEKEND_PREMIUM;
        }

        price *= getSeasonalMultiplier(date);

        if (isHoliday(date)) {
            price *= HOLIDAY_PREMIUM;
        }

        price *= getDemandMultiplier(roomId, date);

        return price;
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

        return nights > 0 ? totalPrice / nights : 0.0;
    }

    private boolean isValidPricingRequest(String roomId, LocalDate from, LocalDate to) {
        return roomId != null && from != null && to != null &&
                from.isBefore(to) && ChronoUnit.DAYS.between(from, to) > 0;
    }

    private double calculateNightlyRates(String roomId, LocalDate from, LocalDate to) {
        double totalPrice = 0.0;
        LocalDate currentDate = from;

        while (currentDate.isBefore(to)) {
            totalPrice += calculateNightPrice(roomId, currentDate);
            currentDate = currentDate.plusDays(1);
        }

        return totalPrice;
    }

    private double getBaseRate(String roomId) {
        if (roomRepository != null) {
            Optional<Room> room = roomRepository.findById(roomId);
            if (room.isPresent()) {
                return room.get().getBasePrice();
            }
        }
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

        if (month >= 12 || month <= 2) return "winter";
        if (month >= 3 && month <= 5) return "spring";
        if (month >= 6 && month <= 8) return "summer";
        return "autumn";
    }

    private boolean isHoliday(LocalDate date) {
        return holidayDates.contains(date);
    }

    private double getDemandMultiplier(String roomId, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        if (dayOfWeek == DayOfWeek.FRIDAY) return 1.2;
        if (dayOfWeek == DayOfWeek.SATURDAY) return 1.25;
        if (dayOfWeek == DayOfWeek.TUESDAY || dayOfWeek == DayOfWeek.WEDNESDAY) return 0.9;
        if (roomId.contains("suite") || roomId.contains("premium")) return 1.1;

        return 1.0;
    }

    private double applyLongStayDiscount(double totalPrice, long nights) {
        if (nights >= 28) return totalPrice * LONG_STAY_MONTHLY_DISCOUNT;
        if (nights >= 14) return totalPrice * LONG_STAY_BIWEEKLY_DISCOUNT;
        if (nights >= 7) return totalPrice * LONG_STAY_WEEKLY_DISCOUNT;
        return totalPrice;
    }

    private double applyEarlyBookingDiscount(double totalPrice, LocalDate checkIn) {
        long daysInAdvance = ChronoUnit.DAYS.between(LocalDate.now(), checkIn);

        if (daysInAdvance >= 90) return totalPrice * EARLY_BOOKING_90_DAYS_DISCOUNT;
        if (daysInAdvance >= 30) return totalPrice * EARLY_BOOKING_30_DAYS_DISCOUNT;

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
        multipliers.put("winter", 0.8);
        multipliers.put("spring", 1.0);
        multipliers.put("summer", 1.4);
        multipliers.put("autumn", 1.1);
        return multipliers;
    }

    private Set<LocalDate> initializeHolidayDates() {
        Set<LocalDate> holidays = new HashSet<>();
        holidays.add(LocalDate.of(2025, 1, 1));
        holidays.add(LocalDate.of(2025, 7, 4));
        holidays.add(LocalDate.of(2025, 12, 25));
        holidays.add(LocalDate.of(2025, 12, 31));
        holidays.add(LocalDate.of(2025, 11, 27));
        holidays.add(LocalDate.of(2025, 2, 14));
        return holidays;
    }

    // Management methods for testing and configuration
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

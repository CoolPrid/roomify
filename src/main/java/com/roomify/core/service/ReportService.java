package com.roomify.core.service;

import com.roomify.core.dto.Booking;
import com.roomify.core.dto.Room;
import com.roomify.core.repository.BookingRepository;
import com.roomify.core.repository.RoomRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    public ReportService(BookingRepository bookingRepository, RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
    }

    public String monthlyReport(int month, int year) {
        if (month < 1 || month > 12) {
            return "ERROR: Invalid month. Must be between 1 and 12.";
        }

        if (year < 2000 || year > 3000) {
            return "ERROR: Invalid year. Must be between 2000 and 3000.";
        }

        YearMonth reportMonth = YearMonth.of(year, month);
        LocalDate startDate = reportMonth.atDay(1);
        LocalDate endDate = reportMonth.atEndOfMonth();

        StringBuilder report = new StringBuilder();

        // Header
        report.append("=".repeat(60)).append("\n");
        report.append(String.format("         ROOMIFY MONTHLY REPORT - %s %d",
                getMonthName(month), year)).append("\n");
        report.append("=".repeat(60)).append("\n\n");

        // Get all bookings for the month
        List<Booking> monthlyBookings = getBookingsInDateRange(startDate, endDate);

        // Revenue Summary
        appendRevenueSummary(report, monthlyBookings);

        // Booking Statistics
        appendBookingStatistics(report, monthlyBookings, reportMonth);

        // Room Performance
        appendRoomPerformance(report, monthlyBookings, startDate, endDate);

        // Top Customers
        appendTopCustomers(report, monthlyBookings);

        // Daily Breakdown
        appendDailyBreakdown(report, monthlyBookings, reportMonth);

        report.append("\n").append("=".repeat(60)).append("\n");
        report.append("Report generated on: ").append(LocalDate.now()).append("\n");
        report.append("=".repeat(60));

        return report.toString();
    }

    public Map<String, Object> getMonthlyMetrics(int month, int year) {
        YearMonth reportMonth = YearMonth.of(year, month);
        LocalDate startDate = reportMonth.atDay(1);
        LocalDate endDate = reportMonth.atEndOfMonth();

        List<Booking> monthlyBookings = getBookingsInDateRange(startDate, endDate);

        Map<String, Object> metrics = new HashMap<>();

        // Revenue metrics
        double totalRevenue = monthlyBookings.stream().mapToDouble(Booking::getPrice).sum();
        double averageBookingValue = monthlyBookings.isEmpty() ? 0 : totalRevenue / monthlyBookings.size();

        metrics.put("totalRevenue", totalRevenue);
        metrics.put("averageBookingValue", averageBookingValue);
        metrics.put("totalBookings", monthlyBookings.size());

        // Occupancy metrics
        int totalRoomNights = calculateTotalRoomNights(startDate, endDate);
        int bookedRoomNights = calculateBookedRoomNights(monthlyBookings);
        double occupancyRate = totalRoomNights > 0 ? (double) bookedRoomNights / totalRoomNights : 0;

        metrics.put("occupancyRate", occupancyRate);
        metrics.put("totalRoomNights", totalRoomNights);
        metrics.put("bookedRoomNights", bookedRoomNights);

        // Customer metrics
        Set<String> uniqueCustomers = monthlyBookings.stream()
                .map(Booking::getUserId)
                .collect(Collectors.toSet());

        metrics.put("uniqueCustomers", uniqueCustomers.size());

        return metrics;
    }

    public String roomPerformanceReport(String roomId, int month, int year) {
        YearMonth reportMonth = YearMonth.of(year, month);
        LocalDate startDate = reportMonth.atDay(1);
        LocalDate endDate = reportMonth.atEndOfMonth();

        List<Booking> roomBookings = getBookingsInDateRange(startDate, endDate).stream()
                .filter(booking -> roomId.equals(booking.getRoomId()))
                .collect(Collectors.toList());

        StringBuilder report = new StringBuilder();

        report.append("Room Performance Report: ").append(roomId).append("\n");
        report.append("Period: ").append(getMonthName(month)).append(" ").append(year).append("\n");
        report.append("-".repeat(50)).append("\n");

        if (roomBookings.isEmpty()) {
            report.append("No bookings found for this room in the specified period.\n");
            return report.toString();
        }

        double totalRevenue = roomBookings.stream().mapToDouble(Booking::getPrice).sum();
        int totalNights = roomBookings.stream()
                .mapToInt(booking -> (int) java.time.temporal.ChronoUnit.DAYS.between(booking.getFrom(), booking.getTo()))
                .sum();

        int daysInMonth = reportMonth.lengthOfMonth();
        double occupancyRate = (double) totalNights / daysInMonth;

        report.append(String.format("Total Revenue: $%.2f\n", totalRevenue));
        report.append(String.format("Total Bookings: %d\n", roomBookings.size()));
        report.append(String.format("Total Nights Booked: %d\n", totalNights));
        report.append(String.format("Occupancy Rate: %.1f%%\n", occupancyRate * 100));
        report.append(String.format("Average Booking Value: $%.2f\n", totalRevenue / roomBookings.size()));

        return report.toString();
    }

    private List<Booking> getBookingsInDateRange(LocalDate startDate, LocalDate endDate) {
        if (bookingRepository == null) {
            return generateMockBookings(startDate, endDate);
        }

        // In a real implementation, this would query the repository
        // For now, we'll generate mock data
        return generateMockBookings(startDate, endDate);
    }

    private List<Booking> generateMockBookings(LocalDate startDate, LocalDate endDate) {
        List<Booking> mockBookings = new ArrayList<>();
        Random random = new Random(startDate.hashCode()); // Deterministic for testing

        String[] roomIds = {"room-1", "room-2", "suite-1", "premium-suite", "economy-room"};
        String[] userIds = {"user-1", "user-2", "user-3", "user-4", "user-5", "user-6"};

        // Generate 10-30 mock bookings for the month
        int numBookings = 10 + random.nextInt(21);

        for (int i = 0; i < numBookings; i++) {
            LocalDate bookingStart = startDate.plusDays(random.nextInt((int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate)));
            LocalDate bookingEnd = bookingStart.plusDays(1 + random.nextInt(7)); // 1-7 night stays

            // Ensure booking doesn't extend beyond our report period
            if (bookingEnd.isAfter(endDate)) {
                bookingEnd = endDate;
            }

            if (!bookingStart.equals(bookingEnd)) {
                Booking booking = new Booking();
                booking.setId("booking-" + i);
                booking.setRoomId(roomIds[random.nextInt(roomIds.length)]);
                booking.setUserId(userIds[random.nextInt(userIds.length)]);
                booking.setFrom(bookingStart);
                booking.setTo(bookingEnd);

                // Calculate price based on nights and room type
                long nights = java.time.temporal.ChronoUnit.DAYS.between(bookingStart, bookingEnd);
                double basePrice = booking.getRoomId().contains("suite") ? 200.0 : 100.0;
                booking.setPrice(nights * basePrice + random.nextDouble() * 50);

                mockBookings.add(booking);
            }
        }

        return mockBookings;
    }

    private void appendRevenueSummary(StringBuilder report, List<Booking> bookings) {
        double totalRevenue = bookings.stream().mapToDouble(Booking::getPrice).sum();
        double averageBooking = bookings.isEmpty() ? 0 : totalRevenue / bookings.size();

        report.append("REVENUE SUMMARY\n");
        report.append("-".repeat(20)).append("\n");
        report.append(String.format("Total Revenue: $%.2f\n", totalRevenue));
        report.append(String.format("Total Bookings: %d\n", bookings.size()));
        report.append(String.format("Average Booking Value: $%.2f\n", averageBooking));
        report.append("\n");
    }

    private void appendBookingStatistics(StringBuilder report, List<Booking> bookings, YearMonth month) {
        if (bookings.isEmpty()) {
            report.append("BOOKING STATISTICS\n");
            report.append("-".repeat(20)).append("\n");
            report.append("No bookings found for this period.\n\n");
            return;
        }

        // Calculate average stay length
        double averageStayLength = bookings.stream()
                .mapToLong(booking -> java.time.temporal.ChronoUnit.DAYS.between(booking.getFrom(), booking.getTo()))
                .average()
                .orElse(0);

        // Find peak booking days
        Map<LocalDate, Long> bookingsByDay = bookings.stream()
                .flatMap(booking -> booking.getFrom().datesUntil(booking.getTo()))
                .collect(Collectors.groupingBy(date -> date, Collectors.counting()));

        Optional<Map.Entry<LocalDate, Long>> peakDay = bookingsByDay.entrySet().stream()
                .max(Map.Entry.comparingByValue());

        report.append("BOOKING STATISTICS\n");
        report.append("-".repeat(20)).append("\n");
        report.append(String.format("Average Stay Length: %.1f nights\n", averageStayLength));
        report.append(String.format("Shortest Stay: %d nights\n",
                bookings.stream().mapToInt(b -> (int) java.time.temporal.ChronoUnit.DAYS.between(b.getFrom(), b.getTo())).min().orElse(0)));
        report.append(String.format("Longest Stay: %d nights\n",
                bookings.stream().mapToInt(b -> (int) java.time.temporal.ChronoUnit.DAYS.between(b.getFrom(), b.getTo())).max().orElse(0)));

        if (peakDay.isPresent()) {
            report.append(String.format("Peak Occupancy Day: %s (%d rooms occupied)\n",
                    peakDay.get().getKey(), peakDay.get().getValue()));
        }
        report.append("\n");
    }

    private void appendRoomPerformance(StringBuilder report, List<Booking> bookings, LocalDate startDate, LocalDate endDate) {
        Map<String, List<Booking>> bookingsByRoom = bookings.stream()
                .collect(Collectors.groupingBy(Booking::getRoomId));

        report.append("ROOM PERFORMANCE\n");
        report.append("-".repeat(20)).append("\n");
        report.append(String.format("%-15s %10s %10s %12s\n", "Room ID", "Bookings", "Revenue", "Occupancy"));
        report.append("-".repeat(50)).append("\n");

        int daysInPeriod = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);

        for (Map.Entry<String, List<Booking>> entry : bookingsByRoom.entrySet()) {
            String roomId = entry.getKey();
            List<Booking> roomBookings = entry.getValue();

            double roomRevenue = roomBookings.stream().mapToDouble(Booking::getPrice).sum();
            int totalNights = roomBookings.stream()
                    .mapToInt(booking -> (int) java.time.temporal.ChronoUnit.DAYS.between(booking.getFrom(), booking.getTo()))
                    .sum();
            double occupancyRate = (double) totalNights / daysInPeriod * 100;

            report.append(String.format("%-15s %10d $%9.2f %10.1f%%\n",
                    roomId, roomBookings.size(), roomRevenue, occupancyRate));
        }
        report.append("\n");
    }

    private void appendTopCustomers(StringBuilder report, List<Booking> bookings) {
        Map<String, Double> revenueByCustomer = bookings.stream()
                .collect(Collectors.groupingBy(Booking::getUserId,
                        Collectors.summingDouble(Booking::getPrice)));

        List<Map.Entry<String, Double>> topCustomers = revenueByCustomer.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        report.append("TOP CUSTOMERS\n");
        report.append("-".repeat(20)).append("\n");
        report.append(String.format("%-15s %10s %10s\n", "Customer ID", "Bookings", "Revenue"));
        report.append("-".repeat(40)).append("\n");

        for (Map.Entry<String, Double> entry : topCustomers) {
            String customerId = entry.getKey();
            double revenue = entry.getValue();
            long bookingCount = bookings.stream()
                    .filter(booking -> customerId.equals(booking.getUserId()))
                    .count();

            report.append(String.format("%-15s %10d $%9.2f\n",
                    customerId, bookingCount, revenue));
        }
        report.append("\n");
    }

    private void appendDailyBreakdown(StringBuilder report, List<Booking> bookings, YearMonth month) {
        Map<LocalDate, List<Booking>> bookingsByDate = new HashMap<>();

        // Initialize all days in the month
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            bookingsByDate.put(date, new ArrayList<>());
        }

        // Populate with actual bookings
        for (Booking booking : bookings) {
            LocalDate current = booking.getFrom();
            while (current.isBefore(booking.getTo())) {
                if (bookingsByDate.containsKey(current)) {
                    bookingsByDate.get(current).add(booking);
                }
                current = current.plusDays(1);
            }
        }

        report.append("DAILY OCCUPANCY BREAKDOWN\n");
        report.append("-".repeat(30)).append("\n");
        report.append("Date       Rooms  Revenue\n");
        report.append("-".repeat(25)).append("\n");

        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            List<Booking> dayBookings = bookingsByDate.get(date);

            Set<String> occupiedRooms = dayBookings.stream()
                    .map(Booking::getRoomId)
                    .collect(Collectors.toSet());

            double dayRevenue = dayBookings.stream()
                    .mapToDouble(booking -> {
                        long totalNights = java.time.temporal.ChronoUnit.DAYS.between(booking.getFrom(), booking.getTo());
                        return totalNights > 0 ? booking.getPrice() / totalNights : 0;
                    })
                    .sum();

            report.append(String.format("%02d/%02d/%d    %2d   $%7.2f\n",
                    day, month.getMonthValue(), month.getYear(),
                    occupiedRooms.size(), dayRevenue));
        }
        report.append("\n");
    }

    private int calculateTotalRoomNights(LocalDate startDate, LocalDate endDate) {
        int daysInPeriod = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        int totalRooms = 5; // Assume we have 5 rooms total
        return daysInPeriod * totalRooms;
    }

    private int calculateBookedRoomNights(List<Booking> bookings) {
        return bookings.stream()
                .mapToInt(booking -> (int) java.time.temporal.ChronoUnit.DAYS.between(booking.getFrom(), booking.getTo()))
                .sum();
    }

    private String getMonthName(int month) {
        String[] months = {
                "", "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        return months[month];
    }

    // Additional report methods
    public String yearlyReport(int year) {
        StringBuilder report = new StringBuilder();

        report.append("=".repeat(60)).append("\n");
        report.append(String.format("              ROOMIFY YEARLY REPORT - %d", year)).append("\n");
        report.append("=".repeat(60)).append("\n\n");

        double yearlyRevenue = 0;
        int yearlyBookings = 0;

        report.append("MONTHLY BREAKDOWN\n");
        report.append("-".repeat(20)).append("\n");
        report.append(String.format("%-10s %10s %10s %12s\n", "Month", "Bookings", "Revenue", "Avg/Booking"));
        report.append("-".repeat(45)).append("\n");

        for (int month = 1; month <= 12; month++) {
            Map<String, Object> metrics = getMonthlyMetrics(month, year);
            double monthRevenue = (Double) metrics.get("totalRevenue");
            int monthBookings = (Integer) metrics.get("totalBookings");
            double avgBooking = (Double) metrics.get("averageBookingValue");

            yearlyRevenue += monthRevenue;
            yearlyBookings += monthBookings;

            report.append(String.format("%-10s %10d $%9.2f $%11.2f\n",
                    getMonthName(month), monthBookings, monthRevenue, avgBooking));
        }

        report.append("-".repeat(45)).append("\n");
        report.append(String.format("%-10s %10d $%9.2f $%11.2f\n",
                "TOTAL", yearlyBookings, yearlyRevenue,
                yearlyBookings > 0 ? yearlyRevenue / yearlyBookings : 0));

        return report.toString();
    }

    public String occupancyReport(int month, int year) {
        YearMonth reportMonth = YearMonth.of(year, month);
        LocalDate startDate = reportMonth.atDay(1);
        LocalDate endDate = reportMonth.atEndOfMonth();

        List<Booking> bookings = getBookingsInDateRange(startDate, endDate);

        StringBuilder report = new StringBuilder();
        report.append(String.format("OCCUPANCY REPORT - %s %d\n", getMonthName(month), year));
        report.append("-".repeat(40)).append("\n");

        int totalRoomNights = calculateTotalRoomNights(startDate, endDate);
        int bookedRoomNights = calculateBookedRoomNights(bookings);
        double occupancyRate = totalRoomNights > 0 ? (double) bookedRoomNights / totalRoomNights * 100 : 0;

        report.append(String.format("Total Available Room-Nights: %d\n", totalRoomNights));
        report.append(String.format("Total Booked Room-Nights: %d\n", bookedRoomNights));
        report.append(String.format("Overall Occupancy Rate: %.1f%%\n", occupancyRate));
        report.append(String.format("Revenue Per Available Room: $%.2f\n",
                bookings.stream().mapToDouble(Booking::getPrice).sum() / (totalRoomNights / reportMonth.lengthOfMonth())));

        return report.toString();
    }
}
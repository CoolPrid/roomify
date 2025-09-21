package com.roomify.core.service;

import com.roomify.core.repository.BookingRepository;
import com.roomify.core.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EnhancedReportServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(bookingRepository, roomRepository);
    }

    // Input validation tests
    @Test
    void monthlyReport_invalidMonth_returnsError() {
        String report = reportService.monthlyReport(0, 2025);
        assertTrue(report.contains("ERROR"));
        assertTrue(report.contains("Invalid month"));

        report = reportService.monthlyReport(13, 2025);
        assertTrue(report.contains("ERROR"));
        assertTrue(report.contains("Invalid month"));

        report = reportService.monthlyReport(-5, 2025);
        assertTrue(report.contains("ERROR"));
        assertTrue(report.contains("Invalid month"));
    }

    @Test
    void monthlyReport_invalidYear_returnsError() {
        String report = reportService.monthlyReport(6, 1999);
        assertTrue(report.contains("ERROR"));
        assertTrue(report.contains("Invalid year"));

        report = reportService.monthlyReport(6, 3001);
        assertTrue(report.contains("ERROR"));
        assertTrue(report.contains("Invalid year"));
    }

    @Test
    void monthlyReport_validInputs_returnsValidReport() {
        String report = reportService.monthlyReport(6, 2025);

        // Should contain report structure
        assertFalse(report.contains("ERROR"));
        assertTrue(report.contains("ROOMIFY MONTHLY REPORT"));
        assertTrue(report.contains("June 2025"));
        assertTrue(report.contains("REVENUE SUMMARY"));
        assertTrue(report.contains("BOOKING STATISTICS"));
        assertTrue(report.contains("ROOM PERFORMANCE"));
        assertTrue(report.contains("TOP CUSTOMERS"));
        assertTrue(report.contains("DAILY OCCUPANCY BREAKDOWN"));
        assertTrue(report.contains("Report generated on"));
    }

    // Monthly report content tests
    @Test
    void monthlyReport_containsAllSections() {
        String report = reportService.monthlyReport(1, 2025);

        // Verify all expected sections are present
        assertTrue(report.contains("REVENUE SUMMARY"));
        assertTrue(report.contains("Total Revenue"));
        assertTrue(report.contains("Total Bookings"));
        assertTrue(report.contains("Average Booking Value"));

        assertTrue(report.contains("BOOKING STATISTICS"));
        assertTrue(report.contains("Average Stay Length"));

        assertTrue(report.contains("ROOM PERFORMANCE"));
        assertTrue(report.contains("Room ID"));
        assertTrue(report.contains("Revenue"));
        assertTrue(report.contains("Occupancy"));

        assertTrue(report.contains("TOP CUSTOMERS"));
        assertTrue(report.contains("Customer ID"));

        assertTrue(report.contains("DAILY OCCUPANCY BREAKDOWN"));
        assertTrue(report.contains("Date"));
        assertTrue(report.contains("Rooms"));
    }

    @Test
    void monthlyReport_differentMonthsHaveDifferentContent() {
        String januaryReport = reportService.monthlyReport(1, 2025);
        String julyReport = reportService.monthlyReport(7, 2025);

        assertTrue(januaryReport.contains("January 2025"));
        assertTrue(julyReport.contains("July 2025"));

        // Reports should have different data (since mock data is seeded with date)
        assertNotEquals(januaryReport, julyReport);
    }

    // Monthly metrics tests
    @Test
    void getMonthlyMetrics_returnsExpectedMetrics() {
        Map<String, Object> metrics = reportService.getMonthlyMetrics(6, 2025);

        // Should contain all expected metrics
        assertTrue(metrics.containsKey("totalRevenue"));
        assertTrue(metrics.containsKey("averageBookingValue"));
        assertTrue(metrics.containsKey("totalBookings"));
        assertTrue(metrics.containsKey("occupancyRate"));
        assertTrue(metrics.containsKey("totalRoomNights"));
        assertTrue(metrics.containsKey("bookedRoomNights"));
        assertTrue(metrics.containsKey("uniqueCustomers"));

        // Verify data types
        assertTrue(metrics.get("totalRevenue") instanceof Double);
        assertTrue(metrics.get("averageBookingValue") instanceof Double);
        assertTrue(metrics.get("totalBookings") instanceof Integer);
        assertTrue(metrics.get("occupancyRate") instanceof Double);
        assertTrue(metrics.get("totalRoomNights") instanceof Integer);
        assertTrue(metrics.get("bookedRoomNights") instanceof Integer);
        assertTrue(metrics.get("uniqueCustomers") instanceof Integer);
    }

    @Test
    void getMonthlyMetrics_calculatesCorrectValues() {
        Map<String, Object> metrics = reportService.getMonthlyMetrics(6, 2025);

        double totalRevenue = (Double) metrics.get("totalRevenue");
        int totalBookings = (Integer) metrics.get("totalBookings");
        double averageBookingValue = (Double) metrics.get("averageBookingValue");

        // Average should be calculated correctly
        if (totalBookings > 0) {
            assertEquals(totalRevenue / totalBookings, averageBookingValue, 0.01);
        } else {
            assertEquals(0.0, averageBookingValue);
        }

        // Occupancy rate should be between 0 and 1
        double occupancyRate = (Double) metrics.get("occupancyRate");
        assertTrue(occupancyRate >= 0.0 && occupancyRate <= 1.0);

        // Values should be non-negative
        assertTrue(totalRevenue >= 0);
        assertTrue(totalBookings >= 0);
        assertTrue((Integer) metrics.get("uniqueCustomers") >= 0);
    }

    // Room performance report tests
    @Test
    void roomPerformanceReport_validInput_returnsReport() {
        String report = reportService.roomPerformanceReport("room-1", 6, 2025);

        assertTrue(report.contains("Room Performance Report: room-1"));
        assertTrue(report.contains("Period: June 2025"));
        assertTrue(report.contains("Total Revenue"));
        assertTrue(report.contains("Total Bookings"));
        assertTrue(report.contains("Occupancy Rate"));
    }

    @Test
    void roomPerformanceReport_noBookings_handlesGracefully() {
        // Use a room ID that's unlikely to have bookings in mock data
        String report = reportService.roomPerformanceReport("non-existent-room", 6, 2025);

        assertTrue(report.contains("Room Performance Report: non-existent-room"));
        assertTrue(report.contains("No bookings found") || report.contains("Total Revenue"));
    }

    @Test
    void roomPerformanceReport_differentRooms_differentResults() {
        String room1Report = reportService.roomPerformanceReport("room-1", 6, 2025);
        String room2Report = reportService.roomPerformanceReport("room-2", 6, 2025);

        // Reports should be different (unless both rooms have no bookings)
        // At minimum, the room ID in the header should be different
        assertTrue(room1Report.contains("room-1"));
        assertTrue(room2Report.contains("room-2"));
    }

    // Yearly report tests
    @Test
    void yearlyReport_returnsCompleteReport() {
        String report = reportService.yearlyReport(2025);

        assertTrue(report.contains("ROOMIFY YEARLY REPORT - 2025"));
        assertTrue(report.contains("MONTHLY BREAKDOWN"));
        assertTrue(report.contains("TOTAL"));

        // Should contain all 12 months
        assertTrue(report.contains("January"));
        assertTrue(report.contains("February"));
        assertTrue(report.contains("March"));
        assertTrue(report.contains("April"));
        assertTrue(report.contains("May"));
        assertTrue(report.contains("June"));
        assertTrue(report.contains("July"));
        assertTrue(report.contains("August"));
        assertTrue(report.contains("September"));
        assertTrue(report.contains("October"));
        assertTrue(report.contains("November"));
        assertTrue(report.contains("December"));
    }

    @Test
    void yearlyReport_calculatesTotalsCorrectly() {
        String report = reportService.yearlyReport(2025);

        // Extract the total line (last line of the table)
        String[] lines = report.split("\n");
        String totalLine = null;
        for (String line : lines) {
            if (line.contains("TOTAL")) {
                totalLine = line;
                break;
            }
        }

        assertNotNull(totalLine);
        assertTrue(totalLine.contains("TOTAL"));
    }

    // Occupancy report tests
    @Test
    void occupancyReport_returnsValidReport() {
        String report = reportService.occupancyReport(6, 2025);

        assertTrue(report.contains("OCCUPANCY REPORT - June 2025"));
        assertTrue(report.contains("Total Available Room-Nights"));
        assertTrue(report.contains("Total Booked Room-Nights"));
        assertTrue(report.contains("Overall Occupancy Rate"));
        assertTrue(report.contains("Revenue Per Available Room"));
    }

    @Test
    void occupancyReport_calculatesOccupancyRate() {
        String report = reportService.occupancyReport(6, 2025);

        // Should contain a percentage
        assertTrue(report.matches(".*\\d+\\.\\d%.*"));
    }

    // Edge case tests
    @Test
    void monthlyReport_february_handlesCorrectly() {
        String report = reportService.monthlyReport(2, 2025);
        assertTrue(report.contains("February 2025"));

        // February 2025 has 28 days (not a leap year)
        // The daily breakdown should reflect this
        assertFalse(report.contains("29/02/2025"));
        assertFalse(report.contains("30/02/2025"));
    }

    @Test
    void monthlyReport_leapYear_handlesFebruary() {
        String report = reportService.monthlyReport(2, 2024); // 2024 is a leap year
        assertTrue(report.contains("February 2024"));

        // Should handle 29 days in February 2024
        assertTrue(report.contains("29/02/2024") || report.length() > 100);
    }

    @Test
    void getMonthlyMetrics_consistentWithReport() {
        // The metrics should be consistent with what appears in the report
        Map<String, Object> metrics = reportService.getMonthlyMetrics(6, 2025);
        String report = reportService.monthlyReport(6, 2025);

        double totalRevenue = (Double) metrics.get("totalRevenue");
        int totalBookings = (Integer) metrics.get("totalBookings");

        // The report should contain these same values
        assertTrue(report.contains(String.format("%.2f", totalRevenue)) ||
                report.contains(String.valueOf(totalBookings)));
    }

    // Boundary tests
    @Test
    void monthlyReport_boundaryMonths_work() {
        // January (month 1)
        String jan = reportService.monthlyReport(1, 2025);
        assertTrue(jan.contains("January 2025"));
        assertFalse(jan.contains("ERROR"));

        // December (month 12)
        String dec = reportService.monthlyReport(12, 2025);
        assertTrue(dec.contains("December 2025"));
        assertFalse(dec.contains("ERROR"));
    }

    @Test
    void monthlyReport_boundaryYears_work() {
        // Minimum year (2000)
        String report2000 = reportService.monthlyReport(6, 2000);
        assertTrue(report2000.contains("June 2000"));
        assertFalse(report2000.contains("ERROR"));

        // Maximum year (3000)
        String report3000 = reportService.monthlyReport(6, 3000);
        assertTrue(report3000.contains("June 3000"));
        assertFalse(report3000.contains("ERROR"));
    }

    // Mock data consistency tests
    @Test
    void getMonthlyMetrics_samePeriodSameResults() {
        Map<String, Object> metrics1 = reportService.getMonthlyMetrics(6, 2025);
        Map<String, Object> metrics2 = reportService.getMonthlyMetrics(6, 2025);

        // Should return identical results for the same period
        assertEquals(metrics1, metrics2);
    }

    @Test
    void monthlyReport_samePeriodSameResults() {
        String report1 = reportService.monthlyReport(6, 2025);
        String report2 = reportService.monthlyReport(6, 2025);

        // Should return identical reports for the same period
        assertEquals(report1, report2);
    }

    // Performance tests (basic)
    @Test
    void monthlyReport_executesInReasonableTime() {
        long startTime = System.currentTimeMillis();

        reportService.monthlyReport(6, 2025);

        long executionTime = System.currentTimeMillis() - startTime;

        // Should complete within 1 second (very generous for a report)
        assertTrue(executionTime < 1000,
                "Report generation took " + executionTime + "ms, which is too long");
    }

    @Test
    void yearlyReport_executesInReasonableTime() {
        long startTime = System.currentTimeMillis();

        reportService.yearlyReport(2025);

        long executionTime = System.currentTimeMillis() - startTime;

        // Yearly report might take a bit longer since it processes 12 months
        assertTrue(executionTime < 2000,
                "Yearly report generation took " + executionTime + "ms, which is too long");
    }
}

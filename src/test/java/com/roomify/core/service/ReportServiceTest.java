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
class ReportServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(bookingRepository, roomRepository);
    }

    @Test
    void monthlyReport_validInput_returnsReport() {
        String report = reportService.monthlyReport(6, 2025);

        assertNotNull(report);
        assertTrue(report.contains("ROOMIFY MONTHLY REPORT"));
        assertTrue(report.contains("June 2025"));
        assertTrue(report.contains("REVENUE SUMMARY"));
    }

    @Test
    void monthlyReport_invalidMonth_returnsError() {
        String report = reportService.monthlyReport(13, 2025);

        assertTrue(report.contains("ERROR"));
        assertTrue(report.contains("Invalid month"));
    }

    @Test
    void monthlyReport_invalidYear_returnsError() {
        String report = reportService.monthlyReport(6, 1999);

        assertTrue(report.contains("ERROR"));
        assertTrue(report.contains("Invalid year"));
    }

    @Test
    void getMonthlyMetrics_returnsCorrectMetrics() {
        Map<String, Object> metrics = reportService.getMonthlyMetrics(6, 2025);

        assertNotNull(metrics);
        assertTrue(metrics.containsKey("totalRevenue"));
        assertTrue(metrics.containsKey("totalBookings"));
        assertTrue(metrics.containsKey("averageBookingValue"));
        assertTrue(metrics.containsKey("occupancyRate"));
        assertTrue(metrics.containsKey("uniqueCustomers"));
    }

    @Test
    void roomPerformanceReport_returnsReport() {
        String report = reportService.roomPerformanceReport("room-1", 6, 2025);

        assertNotNull(report);
        assertTrue(report.contains("Room Performance Report: room-1"));
        assertTrue(report.contains("June 2025"));
    }

    @Test
    void yearlyReport_returnsReport() {
        String report = reportService.yearlyReport(2025);

        assertNotNull(report);
        assertTrue(report.contains("ROOMIFY YEARLY REPORT - 2025"));
        assertTrue(report.contains("MONTHLY BREAKDOWN"));
    }

    @Test
    void occupancyReport_returnsReport() {
        String report = reportService.occupancyReport(6, 2025);

        assertNotNull(report);
        assertTrue(report.contains("OCCUPANCY REPORT"));
        assertTrue(report.contains("June 2025"));
        assertTrue(report.contains("Overall Occupancy Rate"));
    }

    @Test
    void monthlyReport_edgeCases_handledCorrectly() {
        // Test February in leap year
        String report = reportService.monthlyReport(2, 2024);
        assertNotNull(report);
        assertTrue(report.contains("February 2024"));

        // Test December
        report = reportService.monthlyReport(12, 2025);
        assertNotNull(report);
        assertTrue(report.contains("December 2025"));

        // Test January
        report = reportService.monthlyReport(1, 2025);
        assertNotNull(report);
        assertTrue(report.contains("January 2025"));
    }
}
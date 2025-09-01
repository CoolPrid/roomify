package com.roomify.core.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReportAndCancellationTest {

    @Test
    void monthlyReport_returnsReportString() {
        ReportService rs = new ReportService();
        String out = rs.monthlyReport(1, 2025);
        assertNotNull(out);
        assertTrue(out.contains("REPORT"));
    }

    @Test
    void cancellationPolicy_refundAmount_nonNegative() {
        CancellationPolicyService cps = new CancellationPolicyService();
        double r = cps.refundAmount("b1");
        assertTrue(r >= 0.0);
    }
}

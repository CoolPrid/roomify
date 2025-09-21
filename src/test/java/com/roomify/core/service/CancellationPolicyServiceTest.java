package com.roomify.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CancellationPolicyServiceTest {

    private CancellationPolicyService cancellationPolicyService;

    @BeforeEach
    void setUp() {
        cancellationPolicyService = new CancellationPolicyService();
    }

    @Test
    void refundAmount_returnsZeroForNow() {
        double refund = cancellationPolicyService.refundAmount("booking-123");
        assertEquals(0.0, refund);
    }

    @Test
    void refundAmount_handlesNullBookingId() {
        double refund = cancellationPolicyService.refundAmount(null);
        assertEquals(0.0, refund);
    }

    @Test
    void refundAmount_handlesEmptyBookingId() {
        double refund = cancellationPolicyService.refundAmount("");
        assertEquals(0.0, refund);
    }

    @Test
    void refundAmount_multipleCallsConsistent() {
        String bookingId = "booking-456";
        double refund1 = cancellationPolicyService.refundAmount(bookingId);
        double refund2 = cancellationPolicyService.refundAmount(bookingId);

        assertEquals(refund1, refund2);
    }
}

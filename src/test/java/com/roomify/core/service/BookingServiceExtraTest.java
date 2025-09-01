package com.roomify.core.service;

import com.roomify.core.dto.Booking;
import com.roomify.core.dto.BookingRequest;
import com.roomify.core.dto.PaymentResult;
import com.roomify.core.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceExtraTest {

    @Mock BookingRepository bookingRepository;
    @Mock AvailabilityService availabilityService;
    @Mock PaymentService paymentService;
    @Mock BookingValidator bookingValidator;
    @Mock PricingService pricingService;
    @Mock DiscountService discountService;
    @Mock NotificationService notificationService;
    @Mock InvoiceService invoiceService;

    @InjectMocks BookingService bookingService;

    @Test
    void createBooking_whenPaymentFails_throwsIllegalStateException() {
        var req = new BookingRequest("r1","u1", LocalDate.now(), LocalDate.now().plusDays(1));
        when(availabilityService.isAvailable(any(), any(), any())).thenReturn(true);
        when(pricingService.calculatePrice(any(), any(), any())).thenReturn(100.0);
        when(discountService.applyDiscount(any(), anyDouble())).thenReturn(100.0);
        when(paymentService.charge(any(), anyDouble())).thenReturn(new PaymentResult(false, null));

        assertThrows(IllegalStateException.class, () -> bookingService.createBooking(req));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_whenValidatorThrows_propagatesException() {
        var req = new BookingRequest("r1","u1", LocalDate.now(), LocalDate.now().plusDays(2));
        doThrow(new IllegalArgumentException("bad")).when(bookingValidator).validate(any());
        assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(req));
        verify(availabilityService, never()).isAvailable(any(), any(), any());
    }

    @Test
    void cancelBooking_invokesRepositoryDelete() {
        // simply verify that cancelBooking calls delete on repository
        doNothing().when(bookingRepository).delete("b123");
        bookingService.cancelBooking("b123");
        verify(bookingRepository).delete("b123");
    }
}

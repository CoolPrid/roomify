package com.roomify.core.service;

import com.roomify.core.dto.Booking;
import com.roomify.core.dto.BookingRequest;
import com.roomify.core.dto.PaymentResult;
import com.roomify.core.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    BookingRepository bookingRepository;

    @Mock
    AvailabilityService availabilityService;

    @Mock
    PaymentService paymentService;

    @Mock
    BookingValidator bookingValidator;

    @Mock
    PricingService pricingService;

    @Mock
    DiscountService discountService;

    @Mock
    NotificationService notificationService;

    @Mock
    InvoiceService invoiceService;

    @InjectMocks
    BookingService bookingService;

    @Test
    void createBooking_whenRoomAvailable_andPaymentSucceeds_returnsBooking() {
        var req = new BookingRequest("room1","user1", LocalDate.now(), LocalDate.now().plusDays(2));
        when(availabilityService.isAvailable("room1", req.from(), req.to())).thenReturn(true);
        when(pricingService.calculatePrice("room1", req.from(), req.to())).thenReturn(200.0);
        when(discountService.applyDiscount("user1", 200.0)).thenReturn(200.0);
        when(paymentService.charge(any(), anyDouble())).thenReturn(new PaymentResult(true, "tx123"));
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            var b = (Booking) inv.getArgument(0);
            b.setId("b1");
            return b;
        });

        Booking result = bookingService.createBooking(req);

        assertNotNull(result);
        assertEquals("user1", result.getUserId());
        verify(paymentService).charge("user1", 200.0);
        verify(bookingRepository).save(any(Booking.class));
        verify(notificationService).notifyBookingCreated("user1", "b1");
    }

    @Test
    void createBooking_whenRoomNotAvailable_throws() {
        var req = new BookingRequest("room1","user1", LocalDate.now(), LocalDate.now().plusDays(2));
        when(availabilityService.isAvailable("room1", req.from(), req.to())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> bookingService.createBooking(req));
        verify(paymentService, never()).charge(any(), anyDouble());
    }
}

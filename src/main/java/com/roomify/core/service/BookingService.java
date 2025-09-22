package com.roomify.core.service;

import com.roomify.core.dto.Booking;
import com.roomify.core.dto.BookingRequest;
import com.roomify.core.dto.PaymentResult;
import com.roomify.core.repository.BookingRepository;


public class BookingService {

    private final BookingRepository bookingRepository;
    private final AvailabilityService availabilityService;
    private final PaymentService paymentService;
    private final BookingValidator bookingValidator;
    private final PricingService pricingService;
    private final DiscountService discountService;
    private final NotificationService notificationService;
    private final InvoiceService invoiceService;

    public BookingService(BookingRepository bookingRepository,
                          AvailabilityService availabilityService,
                          PaymentService paymentService,
                          BookingValidator bookingValidator,
                          PricingService pricingService,
                          DiscountService discountService,
                          NotificationService notificationService,
                          InvoiceService invoiceService) {
        this.bookingRepository = bookingRepository;
        this.availabilityService = availabilityService;
        this.paymentService = paymentService;
        this.bookingValidator = bookingValidator;
        this.pricingService = pricingService;
        this.discountService = discountService;
        this.notificationService = notificationService;
        this.invoiceService = invoiceService;
    }

    public Booking createBooking(BookingRequest request) {
        bookingValidator.validate(request);

        if (!availabilityService.isAvailable(request.roomId(), request.from(), request.to())) {
            throw new IllegalArgumentException("Room not available");
        }

        double basePrice = pricingService.calculatePrice(request.roomId(), request.from(), request.to());
        double finalPrice = discountService.applyDiscount(request.userId(), basePrice);

        PaymentResult payment = paymentService.charge(request.userId(), finalPrice);
        if (!payment.isSuccess()) {
            throw new IllegalStateException("Payment failed");
        }

        Booking booking = createBookingEntity(request, finalPrice);
        Booking savedBooking = bookingRepository.save(booking);

        processPostBookingTasks(request.userId(), savedBooking.getId());

        return savedBooking;
    }


    public void cancelBooking(String bookingId) {
        bookingRepository.delete(bookingId);
    }

    private Booking createBookingEntity(BookingRequest request, double finalPrice) {
        Booking booking = new Booking();
        booking.setRoomId(request.roomId());
        booking.setUserId(request.userId());
        booking.setFrom(request.from());
        booking.setTo(request.to());
        booking.setPrice(finalPrice);
        return booking;
    }

    private void processPostBookingTasks(String userId, String bookingId) {
        notificationService.notifyBookingCreated(userId, bookingId);
        invoiceService.generateInvoiceId();
    }
}

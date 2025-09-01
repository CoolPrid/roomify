package com.roomify.controller;

import com.roomify.core.dto.BookingRequest;
import com.roomify.core.service.BookingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    public BookingController(BookingService bookingService) { this.bookingService = bookingService; }

    @PostMapping
    public Object create(@RequestBody BookingRequest req) {
        return bookingService.createBooking(req);
    }
}

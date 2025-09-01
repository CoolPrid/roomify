package com.roomify.core.service;

import com.roomify.core.dto.BookingRequest;

public class BookingValidator {
    public void validate(BookingRequest req) {
        if (req.from().isAfter(req.to())) throw new IllegalArgumentException("Invalid dates: from after to");
        long nights = java.time.temporal.ChronoUnit.DAYS.between(req.from(), req.to());
        if (nights <= 0) throw new IllegalArgumentException("Stay must be at least 1 night");
    }
}

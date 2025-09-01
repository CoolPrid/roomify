package com.roomify.core.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PricingService {
    public double calculatePrice(String roomId, LocalDate from, LocalDate to) {
        long nights = ChronoUnit.DAYS.between(from, to);
        return Math.max(0, nights * 100.0);
    }
}

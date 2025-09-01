package com.roomify.core.dto;

import java.time.LocalDate;

public record BookingRequest(String roomId, String userId, LocalDate from, LocalDate to) {}

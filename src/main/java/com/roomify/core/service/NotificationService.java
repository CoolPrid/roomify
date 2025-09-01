package com.roomify.core.service;

public interface NotificationService {
    void notifyBookingCreated(String userId, String bookingId);
}

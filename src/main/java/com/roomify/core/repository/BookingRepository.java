package com.roomify.core.repository;

import com.roomify.core.dto.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(String id);
    List<Booking> findByRoomId(String roomId);
    void delete(String id);
}

package com.roomify.core.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class BookingDtoTest {
    @Test
    void gettersAndSetters_work() {
        Booking b = new Booking();
        b.setId("id1");
        b.setRoomId("r1");
        b.setUserId("u1");
        b.setFrom(LocalDate.of(2025,1,1));
        b.setTo(LocalDate.of(2025,1,3));
        b.setPrice(250.5);

        assertEquals("id1", b.getId());
        assertEquals("r1", b.getRoomId());
        assertEquals("u1", b.getUserId());
        assertEquals(LocalDate.of(2025,1,1), b.getFrom());
        assertEquals(LocalDate.of(2025,1,3), b.getTo());
        assertEquals(250.5, b.getPrice());
    }
}

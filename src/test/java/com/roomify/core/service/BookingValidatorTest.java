package com.roomify.core.service;

import com.roomify.core.dto.BookingRequest;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class BookingValidatorTest {

    private final BookingValidator validator = new BookingValidator();

    @Test
    void validate_whenFromAfterTo_throws() {
        var req = new BookingRequest("r", "u", LocalDate.of(2025,1,10), LocalDate.of(2025,1,5));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(req));
    }

    @Test
    void validate_whenZeroNights_throws() {
        var d = LocalDate.of(2025,2,1);
        var req = new BookingRequest("r", "u", d, d);
        assertThrows(IllegalArgumentException.class, () -> validator.validate(req));
    }

    @Test
    void validate_whenValid_doesNotThrow() {
        var req = new BookingRequest("r","u", LocalDate.of(2025,3,1), LocalDate.of(2025,3,3));
        assertDoesNotThrow(() -> validator.validate(req));
    }
}

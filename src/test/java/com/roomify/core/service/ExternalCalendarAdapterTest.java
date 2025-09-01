package com.roomify.core.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExternalCalendarAdapterTest {

    @Test
    void pushBooking_noException() {
        ExternalCalendarAdapter adapter = new ExternalCalendarAdapter();
        assertDoesNotThrow(() -> adapter.pushBooking("b1"));
    }
}

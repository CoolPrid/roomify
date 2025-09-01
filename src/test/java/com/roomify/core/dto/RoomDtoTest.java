package com.roomify.core.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoomDtoTest {
    @Test
    void gettersAndSetters_work() {
        Room r = new Room("rx","suite",2,199.99);
        assertEquals("rx", r.getId());
        assertEquals("suite", r.getType());
        assertEquals(2, r.getCapacity());
        assertEquals(199.99, r.getBasePrice());
        r.setBasePrice(180.0);
        assertEquals(180.0, r.getBasePrice());
    }
}

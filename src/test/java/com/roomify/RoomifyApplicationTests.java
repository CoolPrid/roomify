package com.roomify;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoomifyApplicationTests {
    @Test
    void trivial() {
        assertTrue(true);
    }

    @Test
    void main_runsWithoutExceptions() {
        assertDoesNotThrow(() -> RoomifyApplication.main(new String[]{}));
    }
}

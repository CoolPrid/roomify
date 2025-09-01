package com.roomify.core.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserDtoTest {
    @Test
    void gettersAndSetters_work() {
        User u = new User("u1","a@b.com","Jan");
        assertEquals("u1", u.getId());
        assertEquals("a@b.com", u.getEmail());
        assertEquals("Jan", u.getName());
        u.setName("Janek");
        assertEquals("Janek", u.getName());
    }
}

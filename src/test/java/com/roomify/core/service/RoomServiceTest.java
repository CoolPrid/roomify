package com.roomify.core.service;

import com.roomify.core.dto.Room;
import com.roomify.core.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock RoomRepository roomRepository;
    @InjectMocks RoomService roomService;

    @Test
    void getRoom_whenExists_returnsRoom() {
        var r = new Room("r1","single",1,100.0);
        when(roomRepository.findById("r1")).thenReturn(Optional.of(r));
        Room got = roomService.getRoom("r1");
        assertEquals("r1", got.getId());
    }

    @Test
    void getRoom_whenMissing_throws() {
        when(roomRepository.findById("x")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> roomService.getRoom("x"));
    }
}

package com.roomify.core.service;

import com.roomify.core.dto.Room;
import com.roomify.core.repository.RoomRepository;

public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room getRoom(String id) {
        return roomRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Room not found"));
    }
}

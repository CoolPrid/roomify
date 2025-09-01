package com.roomify.core.repository;

import com.roomify.core.dto.Room;
import java.util.Optional;

public interface RoomRepository {
    Optional<Room> findById(String id);
    Room save(Room room);
}

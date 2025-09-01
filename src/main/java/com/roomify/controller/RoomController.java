package com.roomify.controller;

import com.roomify.core.service.RoomService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    public RoomController(RoomService roomService) { this.roomService = roomService; }

    @GetMapping("/{id}")
    public Object get(@PathVariable String id) { return roomService.getRoom(id); }
}

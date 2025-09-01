package com.roomify.core.dto;

import java.time.LocalDate;

public class Booking {
    private String id;
    private String roomId;
    private String userId;
    private LocalDate from;
    private LocalDate to;
    private double price;

    public Booking() {}

    public Booking(String id, String roomId, String userId, LocalDate from, LocalDate to, double price) {
        this.id = id;
        this.roomId = roomId;
        this.userId = userId;
        this.from = from;
        this.to = to;
        this.price = price;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public java.time.LocalDate getFrom() { return from; }
    public void setFrom(java.time.LocalDate from) { this.from = from; }

    public java.time.LocalDate getTo() { return to; }
    public void setTo(java.time.LocalDate to) { this.to = to; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}

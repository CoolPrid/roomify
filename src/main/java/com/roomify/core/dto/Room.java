package com.roomify.core.dto;

public class Room {
    private String id;
    private String type;
    private int capacity;
    private double basePrice;

    public Room() {}

    public Room(String id, String type, int capacity, double basePrice) {
        this.id = id;
        this.type = type;
        this.capacity = capacity;
        this.basePrice = basePrice;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
}

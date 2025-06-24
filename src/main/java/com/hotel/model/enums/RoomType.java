package com.hotel.model.enums;

public enum RoomType {
    SINGLE("单人间", 200.0),
    DOUBLE("双人间", 300.0),
    SUITE("套房", 500.0),
    DELUXE("豪华间", 800.0);

    private final String description;
    private final double price;

    RoomType(String description, double price) {
        this.description = description;
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }
}
package com.hotel.model.enums;

public enum RoomType {
    SINGLE("单人间"),
    DOUBLE("双人间"),
    SUITE("套房"),
    DELUXE("豪华间");

    private final String description;

    RoomType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
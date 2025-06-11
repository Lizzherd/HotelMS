package com.hotel.model.enums;

public enum Gender {
    MALE("男性"),
    FEMALE("女性"),
    OTHER("其他");

    private final String description;

    Gender(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
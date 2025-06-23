package com.hotel.model;

import com.hotel.model.enums.RoomType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rooms")
public class Room {
    @Id
    private String id;

    @Indexed(unique = true)
    private String roomNumber; // 客房号

    private double price; // 单价
    private boolean isOccupied; // 是否入住
    private RoomType type; // 类型

    public Room() {
    }

    public Room(String roomNumber, double price, RoomType type) {
        this.roomNumber = roomNumber;
        this.price = price;
        this.type = type;
        this.isOccupied = false; // 新房间默认未入住
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public RoomType getType() {
        return type;
    }

    public void setType(RoomType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id='" + id + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", price=" + price +
                ", isOccupied=" + isOccupied +
                ", type=" + type +
                '}';
    }
}
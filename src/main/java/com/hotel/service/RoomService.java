package com.hotel.service;

import com.hotel.model.Room;
import com.hotel.model.enums.RoomType;
import java.util.List;
import java.util.Optional;

public interface RoomService {
    Room createRoom(Room room);
    Optional<Room> getRoomById(String id);
    Optional<Room> getRoomByRoomNumber(String roomNumber);
    List<Room> getAllRooms();
    List<Room> getAvailableRooms();
    List<Room> getAvailableRoomsByType(RoomType type);
    Room updateRoom(String id, Room roomDetails);
    void deleteRoom(String id);
    boolean roomNumberExists(String roomNumber);
    Room updateRoomOccupancy(String roomId, boolean isOccupied);
    void deleteAllRooms();
}
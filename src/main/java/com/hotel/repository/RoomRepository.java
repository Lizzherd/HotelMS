// src/main/java/com/hotel/repository/RoomRepository.java
package com.hotel.repository;

import com.hotel.model.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface RoomRepository extends MongoRepository<Room, String> {
    List<Room> findByOccupiedFalse();
    Room findByRoomNumber(String roomNumber);
}
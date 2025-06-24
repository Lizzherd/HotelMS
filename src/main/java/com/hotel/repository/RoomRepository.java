package com.hotel.repository;

import com.hotel.model.Room;
import com.hotel.model.enums.RoomType;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends MongoRepository<Room, String> {
    Optional<Room> findByRoomNumber(String roomNumber);
    List<Room> findByIsOccupiedFalse();
    List<Room> findByTypeAndIsOccupiedFalse(RoomType type);
    Optional<Room> findFirstByTypeAndIsOccupiedFalse(RoomType roomType);
    long countByTypeAndIsOccupiedFalse(RoomType roomType);
    boolean existsByRoomNumber(String roomNumber);
}
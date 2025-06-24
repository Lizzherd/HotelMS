package com.hotel.service.impl;

import com.hotel.model.Room;
import com.hotel.model.enums.RoomType;
import com.hotel.repository.RoomRepository;
import com.hotel.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public Room createRoom(Room room) {
        if (roomRepository.existsByRoomNumber(room.getRoomNumber())) {
            throw new IllegalArgumentException("房间号已存在: " + room.getRoomNumber());
        }
        return roomRepository.save(room);
    }

    @Override
    public Optional<Room> getRoomById(String id) {
        return roomRepository.findById(id);
    }

    @Override
    public Optional<Room> getRoomByRoomNumber(String roomNumber) {
        return roomRepository.findByRoomNumber(roomNumber);
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public List<Room> getAvailableRooms() {
        return roomRepository.findByIsOccupiedFalse();
    }

    @Override
    public List<Room> getAvailableRoomsByType(RoomType type) {
        return roomRepository.findByTypeAndIsOccupiedFalse(type);
    }
    @Override
    public long countAvailableRoomsByType(RoomType roomType) {
        return roomRepository.countByTypeAndIsOccupiedFalse(roomType);
    }

    @Override
    public Optional<Room> findFirstAvailableRoomByType(RoomType roomType) {
        return roomRepository.findFirstByTypeAndIsOccupiedFalse(roomType);
    }

    @Override
    public Room updateRoom(String id, Room roomDetails) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("未找到房间ID: " + id));

        if (roomDetails.getRoomNumber() != null && !roomDetails.getRoomNumber().equals(room.getRoomNumber())) {
            if (roomRepository.existsByRoomNumber(roomDetails.getRoomNumber())) {
                throw new IllegalArgumentException("更新的房间号已存在: " + roomDetails.getRoomNumber());
            }
            room.setRoomNumber(roomDetails.getRoomNumber());
        }
        if (roomDetails.getPrice() > 0) room.setPrice(roomDetails.getPrice());
        if (roomDetails.getType() != null) room.setType(roomDetails.getType());


        return roomRepository.save(room);
    }

    @Override
    public void deleteRoom(String id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("未找到房间ID: " + id));
        if (room.isOccupied()) {
            throw new IllegalStateException("无法删除已入住的房间: " + room.getRoomNumber());
        }
        roomRepository.deleteById(id);
    }
    @Override
    public boolean roomNumberExists(String roomNumber) {
        return roomRepository.existsByRoomNumber(roomNumber);
    }

    @Override
    public Room updateRoomOccupancy(String roomId, boolean isOccupied) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("未找到房间ID: " + roomId));
        room.setOccupied(isOccupied);
        return roomRepository.save(room);
    }

    @Override
    @Transactional
    public void deleteAllRooms() {
        roomRepository.deleteAll();
    }
}
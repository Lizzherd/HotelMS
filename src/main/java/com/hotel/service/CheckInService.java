package com.hotel.service;

import com.hotel.model.CheckIn;
import com.hotel.model.Customer; // 确保引入
import com.hotel.model.Room;    // 确保引入
import com.hotel.model.ServiceInfo; // 确保引入

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CheckInService {
    CheckIn processCheckIn(Customer customer, Room room, LocalDate expectedCheckOutDate);

    Optional<CheckIn> getCheckInById(String id);

    Optional<CheckIn> getActiveCheckInById(String id); // <--- **确保这一行存在**

    List<CheckIn> getActiveCheckInsByCustomer(String customerIdCardNumber);

    List<CheckIn> getActiveCheckInsByRoom(String roomNumber);

    List<CheckIn> getAllActiveCheckIns();

    List<CheckIn> getAllCheckInHistory();

    CheckIn addServiceToCheckIn(String checkInId, ServiceInfo serviceInfo, int quantity);

    CheckIn processCheckOut(String checkInId);

    double calculateStayDuration(LocalDate checkInDate, LocalDate checkOutDate);
}
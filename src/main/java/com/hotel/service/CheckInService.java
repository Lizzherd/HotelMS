package com.hotel.service;

import com.hotel.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CheckInService {
    CheckIn processCheckIn(Customer customer, Room room, LocalDate expectedCheckOutDate);
    Optional<CheckIn> getCheckInById(String id);
    List<CheckIn> getActiveCheckInsByCustomer(String customerIdCardNumber);
    List<CheckIn> getActiveCheckInsByRoom(String roomNumber);
    List<CheckIn> getAllActiveCheckIns();
    List<CheckIn> getAllCheckInHistory();
    CheckIn addServiceToCheckIn(String checkInId, ServiceInfo serviceInfo, int quantity);
    CheckIn processCheckOut(String checkInId);
    double calculateStayDuration(LocalDate checkInDate, LocalDate checkOutDate);
}
// src/main/java/com/hotel/service/HotelService.java
package com.hotel.service;

import com.hotel.model.*;

import java.util.List;

public interface HotelService {
    List<Room> getAvailableRooms();
    List<Service> getAllServices();
    CheckInRecord checkIn(Customer customer, String roomNumber, List<ServiceItem> services);
    void addServiceToRecord(String recordId, ServiceItem serviceItem);
    double calculateOccupancyRate();
}
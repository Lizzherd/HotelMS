// src/main/java/com/hotel/service/impl/HotelServiceImpl.java
package com.hotel.service.impl;

import com.hotel.model.*;
import com.hotel.repository.*;
import com.hotel.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HotelServiceImpl implements HotelService {

    @Autowired private RoomRepository roomRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private CheckInRepository checkInRepository;
    @Autowired private MemberRepository memberRepository;

    @Override
    public List<Room> getAvailableRooms() {
        return roomRepository.findByOccupiedFalse();
    }

    @Override
    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    @Transactional
    @Override
    public CheckInRecord checkIn(Customer customer, String roomNumber, List<ServiceItem> services) {
        // 保存或更新客户信息
        Customer existingCustomer = customerRepository.findByIdCard(customer.getIdCard());
        if (existingCustomer != null) {
            customer.setId(existingCustomer.getId());
        }
        Customer savedCustomer = customerRepository.save(customer);

        // 更新会员信息
        if (customer.isMember()) {
            Member member = memberRepository.findByIdCard(customer.getIdCard());
            if (member == null) {
                member = new Member();
                member.setIdCard(customer.getIdCard());
                member.setRegisterDate(LocalDate.now());
                member.setLevel("普通会员");
            }
            memberRepository.save(member);
        }

        // 获取房间并更新状态
        Room room = roomRepository.findByRoomNumber(roomNumber);
        if (room == null) throw new RuntimeException("房间不存在");
        if (room.isOccupied()) throw new RuntimeException("房间已被占用");

        room.setOccupied(true);
        roomRepository.save(room);

        // 创建入住记录
        CheckInRecord record = new CheckInRecord();
        record.setCustomerId(savedCustomer.getId());
        record.setRoomId(room.getId());
        record.setCheckInTime(LocalDateTime.now());

        // 计算服务费用
        for (ServiceItem item : services) {
            Service service = serviceRepository.findById(item.getServiceId()).orElse(null);
            if (service != null) {
                item.setSubtotal(service.getServicePrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }
        }
        record.setServices(services);

        return checkInRepository.save(record);
    }

    @Override
    public void addServiceToRecord(String recordId, ServiceItem serviceItem) {
        CheckInRecord record = checkInRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("入住记录不存在"));

        Service service = serviceRepository.findById(serviceItem.getServiceId())
                .orElseThrow(() -> new RuntimeException("服务不存在"));

        serviceItem.setSubtotal(service.getServicePrice().multiply(BigDecimal.valueOf(serviceItem.getQuantity())));

        record.getServices().add(serviceItem);
        checkInRepository.save(record);
    }

    @Override
    public double calculateOccupancyRate() {
        long totalRooms = roomRepository.count();
        long occupiedRooms = roomRepository.countByOccupiedTrue();
        return totalRooms > 0 ? (double) occupiedRooms / totalRooms * 100 : 0;
    }
}
package com.hotel.service.impl;

import com.hotel.model.*;
import com.hotel.repository.CheckInRepository;
import com.hotel.service.CheckInService;
import com.hotel.service.RoomService; // 引入RoomService
import com.hotel.service.ServiceInfoService; // 引入ServiceInfoService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class CheckInServiceImpl implements CheckInService {

    private final CheckInRepository checkInRepository;
    private final RoomService roomService;
    private final ServiceInfoService serviceInfoService;


    @Autowired
    public CheckInServiceImpl(CheckInRepository checkInRepository, RoomService roomService, ServiceInfoService serviceInfoService) {
        this.checkInRepository = checkInRepository;
        this.roomService = roomService;
        this.serviceInfoService = serviceInfoService;
    }

    @Override
    @Transactional // 保证事务一致性
    public CheckIn processCheckIn(Customer customer, Room room, LocalDate expectedCheckOutDate) {
        if (room.isOccupied()) {
            throw new IllegalStateException("房间 " + room.getRoomNumber() + " 已被占用。");
        }

        // 计算房间费用 (示例：按晚计算，实际应更复杂)
        long nights = ChronoUnit.DAYS.between(LocalDate.now(), expectedCheckOutDate);
        if (nights <= 0) {
            nights = 1; //至少住一晚
        }
        double roomCost = room.getPrice() * nights;


        CheckIn checkIn = new CheckIn(
                customer.getId(),
                customer.getName(),
                customer.getIdCardNumber(),
                room.getId(),
                room.getRoomNumber(),
                expectedCheckOutDate,
                roomCost // 传入计算好的房间总费用
        );
        checkIn.setCheckInDate(LocalDate.now());
        checkIn.setActive(true);

        CheckIn savedCheckIn = checkInRepository.save(checkIn);
        roomService.updateRoomOccupancy(room.getId(), true); // 更新房间状态

        return savedCheckIn;
    }

    @Override
    public Optional<CheckIn> getCheckInById(String id) {
        return checkInRepository.findById(id);
    }
    public Optional<CheckIn> getActiveCheckInById(String id) {
        return checkInRepository.findByIdAndIsActiveTrue(id);
    }


    @Override
    public List<CheckIn> getActiveCheckInsByCustomer(String customerIdCardNumber) {
        return checkInRepository.findByCustomerIdCardNumberAndIsActiveTrue(customerIdCardNumber);
    }

    @Override
    public List<CheckIn> getActiveCheckInsByRoom(String roomNumber) {
        return checkInRepository.findByRoomNumberAndIsActiveTrue(roomNumber);
    }

    @Override
    public List<CheckIn> getAllActiveCheckIns() {
        return checkInRepository.findByIsActiveTrue();
    }

    @Override
    public List<CheckIn> getAllCheckInHistory() {
        return checkInRepository.findAll(); // 或者可以增加一个查询非激活的
    }

    @Override
    @Transactional
    public CheckIn addServiceToCheckIn(String checkInId, com.hotel.model.ServiceInfo serviceInfoModel, int quantity) {
        CheckIn checkIn = getActiveCheckInById(checkInId)
                .orElseThrow(() -> new IllegalArgumentException("未找到有效的入住记录ID: " + checkInId));

        if (quantity <= 0) {
            throw new IllegalArgumentException("服务数量必须大于0");
        }

        CheckInServiceItem item = new CheckInServiceItem(
                serviceInfoModel.getId(),
                serviceInfoModel.getServiceName(),
                quantity,
                serviceInfoModel.getServicePrice()
        );
        checkIn.addServiceItem(item); // addServiceItem内部会调用calculateTotalAmount
        return checkInRepository.save(checkIn);
    }


    @Override
    @Transactional
    public CheckIn processCheckOut(String checkInId) {
        CheckIn checkIn = getActiveCheckInById(checkInId)
                .orElseThrow(() -> new IllegalArgumentException("未找到有效的入住记录ID: " + checkInId));

        // 实际退房日期
        checkIn.setActualCheckOutDate(LocalDate.now());
        checkIn.setActive(false); // 标记为非活动（已退房）

        // 重新计算最终的房间费用（如果之前是预估）
        // 例如，如果入住时按预计天数计算，退房时按实际天数计算
        long actualNights = ChronoUnit.DAYS.between(checkIn.getCheckInDate(), checkIn.getActualCheckOutDate());
        if (actualNights <= 0) actualNights = 1; // 至少算一天

        Room room = roomService.getRoomById(checkIn.getRoomId())
                .orElseThrow(() -> new IllegalStateException("退房时找不到房间信息: " + checkIn.getRoomNumber()));

        checkIn.setRoomCost(room.getPrice() * actualNights); // 更新房间费用
        checkIn.calculateTotalAmount(); // 重新计算总费用

        CheckIn updatedCheckIn = checkInRepository.save(checkIn);
        roomService.updateRoomOccupancy(checkIn.getRoomId(), false); // 更新房间状态为未占用

        return updatedCheckIn;
    }

    @Override
    public double calculateStayDuration(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null || checkOutDate.isBefore(checkInDate)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

}
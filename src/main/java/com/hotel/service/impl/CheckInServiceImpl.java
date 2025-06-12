package com.hotel.service.impl;

import com.hotel.model.*; // 确保引入
import com.hotel.repository.CheckInRepository;
import com.hotel.service.CheckInService;
import com.hotel.service.RoomService;
import com.hotel.service.ServiceInfoService;
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
    private final ServiceInfoService serviceInfoService; // 确保注入

    @Autowired
    public CheckInServiceImpl(CheckInRepository checkInRepository, RoomService roomService, ServiceInfoService serviceInfoService) {
        this.checkInRepository = checkInRepository;
        this.roomService = roomService;
        this.serviceInfoService = serviceInfoService;
    }

    @Override
    @Transactional
    public CheckIn processCheckIn(Customer customer, Room room, LocalDate expectedCheckOutDate) {
        if (room.isOccupied()) {
            throw new IllegalStateException("房间 " + room.getRoomNumber() + " 已被占用。");
        }
        long nights = ChronoUnit.DAYS.between(LocalDate.now(), expectedCheckOutDate);
        if (nights <= 0) {
            nights = 1;
        }
        double roomCost = room.getPrice() * nights;

        CheckIn checkIn = new CheckIn(
                customer.getId(),
                customer.getName(),
                customer.getIdCardNumber(),
                room.getId(),
                room.getRoomNumber(),
                expectedCheckOutDate,
                roomCost
        );
        checkIn.setCheckInDate(LocalDate.now());
        checkIn.setActive(true);

        CheckIn savedCheckIn = checkInRepository.save(checkIn);
        roomService.updateRoomOccupancy(room.getId(), true);

        return savedCheckIn;
    }

    @Override
    public Optional<CheckIn> getCheckInById(String id) {
        return checkInRepository.findById(id);
    }

    @Override // <--- **确保有 @Override 注解**
    public Optional<CheckIn> getActiveCheckInById(String id) {
        return checkInRepository.findByIdAndIsActiveTrue(id); // 假设 CheckInRepository 中有此方法
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
        return checkInRepository.findAll(); // 或者 findByIsActiveFalse()
    }

    @Override
    @Transactional
    public CheckIn addServiceToCheckIn(String checkInId, ServiceInfo serviceInfoModel, int quantity) {
        CheckIn checkIn = getActiveCheckInById(checkInId) // 此处调用本类的方法
                .orElseThrow(() -> new IllegalArgumentException("未找到有效的入住记录ID: " + checkInId + "，或该入住已结束。"));

        if (quantity <= 0) {
            throw new IllegalArgumentException("服务数量必须大于0");
        }

        // ServiceInfo serviceInfoModel = serviceInfoService.getServiceInfoById(serviceInfoId)
        // .orElseThrow(() -> new IllegalArgumentException("无效的服务ID: " + serviceInfoId));
        // ^^^ Controller 现在直接传递 ServiceInfo 对象，所以这行不需要了

        CheckInServiceItem item = new CheckInServiceItem(
                serviceInfoModel.getId(),
                serviceInfoModel.getServiceName(),
                quantity,
                serviceInfoModel.getServicePrice()
        );
        checkIn.addServiceItem(item);
        return checkInRepository.save(checkIn);
    }

    @Override
    @Transactional
    public CheckIn processCheckOut(String checkInId) {
        CheckIn checkIn = getActiveCheckInById(checkInId)
                .orElseThrow(() -> new IllegalArgumentException("未找到有效的入住记录ID: " + checkInId + "，或该入住已结束。"));

        checkIn.setActualCheckOutDate(LocalDate.now());
        checkIn.setActive(false);

        long actualNights = ChronoUnit.DAYS.between(checkIn.getCheckInDate(), checkIn.getActualCheckOutDate());
        if (actualNights <= 0) actualNights = 1;

        Room room = roomService.getRoomById(checkIn.getRoomId())
                .orElseThrow(() -> new IllegalStateException("退房时找不到房间信息: " + checkIn.getRoomNumber()));

        checkIn.setRoomCost(room.getPrice() * actualNights);
        checkIn.calculateTotalAmount();

        CheckIn updatedCheckIn = checkInRepository.save(checkIn);
        roomService.updateRoomOccupancy(checkIn.getRoomId(), false);

        return updatedCheckIn;
    }

    @Override
    public double calculateStayDuration(LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null || checkOutDate.isBefore(checkInDate)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    @Override
    @Transactional
    public void deleteAllCheckIns() {
        checkInRepository.deleteAll();
        // 注意：如果 Room 表中还有 isOccupied 字段，理论上在删除所有 CheckIn 后，
        // 应该将所有 Room 的 isOccupied 更新为 false。
        // 但由于我们也会删除所有 Room，所以这一步可以省略。
    }
}
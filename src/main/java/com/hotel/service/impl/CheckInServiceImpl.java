package com.hotel.service.impl;

import com.hotel.model.*;
import com.hotel.repository.CheckInRepository;
import com.hotel.service.CheckInService;
import com.hotel.service.RoomService;
import com.hotel.service.ServiceInfoService;
import com.hotel.service.ServiceRequestService;
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
    private final ServiceRequestService serviceRequestService;

    @Autowired
    public CheckInServiceImpl(CheckInRepository checkInRepository, RoomService roomService, ServiceInfoService serviceInfoService, ServiceRequestService serviceRequestService) {
        this.checkInRepository = checkInRepository;
        this.roomService = roomService;
        this.serviceInfoService = serviceInfoService;
        this.serviceRequestService = serviceRequestService;
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
        return checkInRepository.findAll();
    }

    @Override
    @Transactional
    public CheckIn addServiceToCheckIn(String checkInId, ServiceInfo serviceInfoModel, int quantity) {
        CheckIn checkIn = getActiveCheckInById(checkInId)
                .orElseThrow(() -> new IllegalArgumentException("未找到有效的入住记录ID: " + checkInId + "，或该入住已结束。"));

        if (quantity <= 0) {
            throw new IllegalArgumentException("服务数量必须大于0");
        }

        // ServiceInfo serviceInfoModel = serviceInfoService.getServiceInfoById(serviceInfoId)
        // .orElseThrow(() -> new IllegalArgumentException("无效的服务ID: " + serviceInfoId));
        // Controller 现在直接传递 ServiceInfo 对象，所以这行不需要了

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
        System.out.println("checkIn.getCheckInDate()：" + checkIn.getCheckInDate());
        System.out.println("checkIn.getActualCheckOutDate()：" + checkIn.getActualCheckOutDate());

        if (actualNights <= 0) actualNights = 1;
        System.out.println("天数：" + actualNights);

        Room room = roomService.getRoomById(checkIn.getRoomId())
                .orElseThrow(() -> new IllegalStateException("退房时找不到房间信息: " + checkIn.getRoomNumber()));

        checkIn.setRoomCost(room.getPrice() * actualNights);
        System.out.println("应收房间金额：" + checkIn.getRoomCost());
        // 统计所有已完成的服务费用
        List<ServiceRequest> completedRequests = serviceRequestService.findByCheckInId(checkInId).stream()
                .filter(req -> "COMPLETED".equals(req.getStatus()))
                .toList();
        double serviceTotal = completedRequests.stream()
                .mapToDouble(req -> req.getSubtotal().doubleValue())
                .sum();
        System.out.println("应收服务总金额：" + serviceTotal);
        // 合计房费和服务费
        checkIn.setTotalAmount(checkIn.getRoomCost() + serviceTotal);

        System.out.println("应收总金额：" + checkIn.getTotalAmount());

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
    }
}
package com.hotel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "check_ins")
public class CheckIn {
    @Id
    private String id; // 入住登记ID

    private String customerId; // 关联的客户ID (
    private String customerName; // 客户姓名
    private String customerIdCardNumber; // 客户身份证号

    private String roomId; // 关联的客房ID
    private String roomNumber; // 客房号

    private LocalDate checkInDate; // 入住日期
    private LocalDate expectedCheckOutDate; // 预计离店日期
    private LocalDate actualCheckOutDate; // 实际离店日期

    private List<CheckInServiceItem> services; // 入住期间选择的服务列表
    private double roomCost; // 房间费用
    private double servicesCost; // 服务总费用
    private double totalAmount; // 总费用
    private boolean isActive; // 是否未退房

    public CheckIn() {
        this.services = new ArrayList<>();
        this.checkInDate = LocalDate.now();
        this.isActive = true;
    }

    public CheckIn(String customerId, String customerName, String customerIdCardNumber, String roomId, String roomNumber, LocalDate expectedCheckOutDate, double roomPricePerNight) {
        this();
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerIdCardNumber = customerIdCardNumber;
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.expectedCheckOutDate = expectedCheckOutDate;
        this.roomCost = roomPricePerNight;
        calculateTotalAmount();
    }

    public void calculateTotalAmount() {
        this.servicesCost = services.stream().mapToDouble(CheckInServiceItem::getSubtotal).sum();
        this.totalAmount = this.roomCost + this.servicesCost;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerIdCardNumber() {
        return customerIdCardNumber;
    }

    public void setCustomerIdCardNumber(String customerIdCardNumber) {
        this.customerIdCardNumber = customerIdCardNumber;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getExpectedCheckOutDate() {
        return expectedCheckOutDate;
    }

    public void setExpectedCheckOutDate(LocalDate expectedCheckOutDate) {
        this.expectedCheckOutDate = expectedCheckOutDate;
    }

    public LocalDate getActualCheckOutDate() {
        return actualCheckOutDate;
    }

    public void setActualCheckOutDate(LocalDate actualCheckOutDate) {
        this.actualCheckOutDate = actualCheckOutDate;
    }

    public List<CheckInServiceItem> getServices() {
        return services;
    }

    public void setServices(List<CheckInServiceItem> services) {
        this.services = services;
        calculateTotalAmount();
    }

    public void addServiceItem(CheckInServiceItem item) {
        this.services.add(item);
        calculateTotalAmount();
    }

    public double getRoomCost() {
        return roomCost;
    }

    public void setRoomCost(double roomCost) {
        this.roomCost = roomCost;
        calculateTotalAmount();
    }

    public double getServicesCost() {
        return servicesCost;
    }

    public void setServicesCost(double servicesCost) {
        this.servicesCost = servicesCost;
        // This setter might not be needed if servicesCost is always derived
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "CheckIn{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", roomNumber='" + roomNumber + '\'' +
                ", checkInDate=" + checkInDate +
                ", totalAmount=" + totalAmount +
                ", isActive=" + isActive +
                '}';
    }
}
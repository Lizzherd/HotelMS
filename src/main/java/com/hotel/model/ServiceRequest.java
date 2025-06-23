package com.hotel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "service_requests")
public class ServiceRequest {
    @Id
    private String id;

    private String checkInId;
    private String customerIdCard;    // 客户身份证号
    private String roomNumber;        // 房间号
    private String serviceInfoId;     // 服务项目ID
    private String serviceName;       // 服务名称
    private int quantity;             // 数量
    private BigDecimal unitPrice;     // 单价，新增
    private LocalDateTime requestTime;// 申请时间
    private String status;            // 三状态APPLIED/COMPLETED/REJECTED
    private String adminNote;         // 管理员备注
    private LocalDateTime completedTime; // 完成时间

    // Getter and Setter methods
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCheckInId() { return checkInId; }
    public void setCheckInId(String checkInId) { this.checkInId = checkInId; }

    public String getCustomerIdCard() { return customerIdCard; }
    public void setCustomerIdCard(String customerIdCard) { this.customerIdCard = customerIdCard; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getServiceInfoId() { return serviceInfoId; }
    public void setServiceInfoId(String serviceInfoId) { this.serviceInfoId = serviceInfoId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public LocalDateTime getRequestTime() { return requestTime; }
    public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public LocalDateTime getCompletedTime() { return completedTime; }
    public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }
    public BigDecimal getSubtotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
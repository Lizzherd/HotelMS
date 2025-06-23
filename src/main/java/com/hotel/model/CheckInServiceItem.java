package com.hotel.model;

// 这个类将作为CheckIn文档的内嵌对象，不单独作为Document
public class CheckInServiceItem {
    private String serviceInfoId; // 关联的服务ID
    private String serviceName;   // 服务名称
    private int quantity;         // 数量
    private double unitPrice;     // 单价
    private double subtotal;      // 小计 (quantity * unitPrice)

    public CheckInServiceItem() {
    }

    public CheckInServiceItem(String serviceInfoId, String serviceName, int quantity, double unitPrice) {
        this.serviceInfoId = serviceInfoId;
        this.serviceName = serviceName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = quantity * unitPrice;
    }

    // Getters and Setters
    public String getServiceInfoId() {
        return serviceInfoId;
    }

    public void setServiceInfoId(String serviceInfoId) {
        this.serviceInfoId = serviceInfoId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.subtotal = quantity * this.unitPrice; // 更新小计
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
        this.subtotal = this.quantity * unitPrice; // 更新小计
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    @Override
    public String toString() {
        return "CheckInServiceItem{" +
                "serviceInfoId='" + serviceInfoId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + subtotal +
                '}';
    }
}
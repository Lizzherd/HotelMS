package com.hotel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "service_info")
public class ServiceInfo {
    @Id
    private String id; // 服务ID (MongoDB自动生成)

    @Indexed(unique = true)
    private String serviceName; // 服务名称 (应唯一)

    private double servicePrice; // 服务单价


    public ServiceInfo() {
    }

    public ServiceInfo(String serviceName, double servicePrice) {
        this.serviceName = serviceName;
        this.servicePrice = servicePrice;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public double getServicePrice() {
        return servicePrice;
    }

    public void setServicePrice(double servicePrice) {
        this.servicePrice = servicePrice;
    }

    @Override
    public String toString() {
        return "ServiceInfo{" +
                "id='" + id + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", servicePrice=" + servicePrice +
                '}';
    }
}
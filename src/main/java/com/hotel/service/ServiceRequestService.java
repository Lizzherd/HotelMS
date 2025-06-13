package com.hotel.service;

import com.hotel.model.ServiceRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ServiceRequestService {
    ServiceRequest applyService(ServiceRequest request);
    List<ServiceRequest> findByCustomerIdCard(String idCard);
    List<ServiceRequest> findByCheckInId(String checkInId);
    List<ServiceRequest> findAll();
    void markCompleted(String requestId, String adminNote);
    void markRejected(String requestId, String adminNote);
    @Transactional
    void deleteAllServiceInfos();
}
package com.hotel.service.impl;

import com.hotel.model.ServiceRequest;
import com.hotel.repository.ServiceRequestRepository;
import com.hotel.service.ServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServiceRequestServiceImpl implements ServiceRequestService {

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Override
    public ServiceRequest applyService(ServiceRequest request) {
        request.setRequestTime(LocalDateTime.now());
        request.setStatus("APPLIED");
        return serviceRequestRepository.save(request);
    }

    @Override
    public List<ServiceRequest> findByCustomerIdCard(String idCard) {
        return serviceRequestRepository.findByCustomerIdCardOrderByRequestTimeDesc(idCard);
    }

    @Override
    public List<ServiceRequest> findByCheckInId(String checkInId) {
        return serviceRequestRepository.findByCheckInIdOrderByRequestTimeDesc(checkInId);
    }

    @Override
    public List<ServiceRequest> findAll() {
        return serviceRequestRepository.findAllByOrderByRequestTimeDesc();
    }

    @Override
    public void markCompleted(String requestId, String adminNote) {
        ServiceRequest req = serviceRequestRepository.findById(requestId).orElseThrow();
        req.setStatus("COMPLETED");
        req.setAdminNote(adminNote);
        req.setCompletedTime(LocalDateTime.now());
        serviceRequestRepository.save(req);
    }

    @Override
    public void markRejected(String requestId, String adminNote) {
        ServiceRequest req = serviceRequestRepository.findById(requestId).orElseThrow();
        req.setStatus("REJECTED");
        req.setAdminNote(adminNote);
        req.setCompletedTime(LocalDateTime.now());
        serviceRequestRepository.save(req);
    }
}
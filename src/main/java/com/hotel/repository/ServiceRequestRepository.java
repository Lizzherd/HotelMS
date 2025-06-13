package com.hotel.repository;

import com.hotel.model.ServiceRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ServiceRequestRepository extends MongoRepository<ServiceRequest, String> {
    List<ServiceRequest> findByCustomerIdCardOrderByRequestTimeDesc(String customerIdCard);
    List<ServiceRequest> findByCheckInIdOrderByRequestTimeDesc(String checkInId);
    List<ServiceRequest> findAllByOrderByRequestTimeDesc();
}
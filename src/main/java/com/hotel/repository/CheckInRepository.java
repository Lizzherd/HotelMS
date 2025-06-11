package com.hotel.repository;

import com.hotel.model.CheckIn;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends MongoRepository<CheckIn, String> {
    List<CheckIn> findByCustomerIdCardNumberAndIsActiveTrue(String idCardNumber);
    List<CheckIn> findByRoomNumberAndIsActiveTrue(String roomNumber);
    List<CheckIn> findByIsActiveTrue();
    List<CheckIn> findByIsActiveFalse();
    Optional<CheckIn> findByIdAndIsActiveTrue(String id);
}
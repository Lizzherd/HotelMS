package com.hotel.repository;

import com.hotel.model.CheckIn;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface CheckInRepository extends MongoRepository<CheckIn, String> {
    List<CheckIn> findByCustomerIdCardNumberAndIsActiveTrue(String idCardNumber);
    List<CheckIn> findByRoomNumberAndIsActiveTrue(String roomNumber);
    List<CheckIn> findByIsActiveTrue();
    List<CheckIn> findByIsActiveFalse(); // 用于查看历史记录中已退房的
    Optional<CheckIn> findByIdAndIsActiveTrue(String id); // <--- **确保这一行存在**
}
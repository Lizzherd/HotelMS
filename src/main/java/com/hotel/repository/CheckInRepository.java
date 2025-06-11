// src/main/java/com/hotel/repository/CheckInRepository.java
package com.hotel.repository;

import com.hotel.model.CheckInRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CheckInRepository extends MongoRepository<CheckInRecord, String> {
    // 自定义查询方法
}
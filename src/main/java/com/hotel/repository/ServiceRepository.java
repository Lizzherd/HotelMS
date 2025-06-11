// src/main/java/com/hotel/repository/ServiceRepository.java
package com.hotel.repository;

import com.hotel.model.Service;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ServiceRepository extends MongoRepository<Service, String> {
    List<Service> findAll();
}
// src/main/java/com/hotel/repository/CustomerRepository.java
package com.hotel.repository;

import com.hotel.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    Customer findByIdCard(String idCard);
}
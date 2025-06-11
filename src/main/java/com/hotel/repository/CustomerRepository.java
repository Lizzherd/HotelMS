package com.hotel.repository;

import com.hotel.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    Optional<Customer> findByIdCardNumber(String idCardNumber);
    boolean existsByIdCardNumber(String idCardNumber);
}
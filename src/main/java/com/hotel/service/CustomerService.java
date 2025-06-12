package com.hotel.service;

import com.hotel.model.Customer;
import java.util.List;
import java.util.Optional;

public interface CustomerService {
    Customer createCustomer(Customer customer);
    Optional<Customer> getCustomerById(String id);
    Optional<Customer> getCustomerByIdCardNumber(String idCardNumber);
    List<Customer> getAllCustomers();
    Customer updateCustomer(String id, Customer customerDetails);
    void deleteCustomer(String id);
    boolean idCardExists(String idCardNumber);
    void deleteAllCustomers();
}
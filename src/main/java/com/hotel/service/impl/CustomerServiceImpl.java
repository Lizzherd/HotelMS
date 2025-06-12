package com.hotel.service.impl;

import com.hotel.model.Customer;
import com.hotel.repository.CustomerRepository;
import com.hotel.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer createCustomer(Customer customer) {
        if (customerRepository.existsByIdCardNumber(customer.getIdCardNumber())) {
            throw new IllegalArgumentException("身份证号已存在: " + customer.getIdCardNumber());
        }
        return customerRepository.save(customer);
    }

    @Override
    public Optional<Customer> getCustomerById(String id) {
        return customerRepository.findById(id);
    }

    @Override
    public Optional<Customer> getCustomerByIdCardNumber(String idCardNumber) {
        return customerRepository.findByIdCardNumber(idCardNumber);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public Customer updateCustomer(String id, Customer customerDetails) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("未找到客户ID: " + id));

        // 检查更新后的身份证号是否与他人重复
        if (customerDetails.getIdCardNumber() != null && !customerDetails.getIdCardNumber().equals(customer.getIdCardNumber())) {
            if (customerRepository.existsByIdCardNumber(customerDetails.getIdCardNumber())) {
                throw new IllegalArgumentException("更新的身份证号已存在: " + customerDetails.getIdCardNumber());
            }
            customer.setIdCardNumber(customerDetails.getIdCardNumber());
        }

        if (customerDetails.getName() != null) customer.setName(customerDetails.getName());
        if (customerDetails.getGender() != null) customer.setGender(customerDetails.getGender());
        if (customerDetails.getContactInfo() != null) customer.setContactInfo(customerDetails.getContactInfo());
        customer.setMember(customerDetails.isMember()); // isMember通常由会员服务管理

        return customerRepository.save(customer);
    }

    @Override
    public void deleteCustomer(String id) {
        if (!customerRepository.existsById(id)) {
            throw new IllegalArgumentException("未找到客户ID: " + id);
        }
        // 注意：删除客户前可能需要检查是否有未完成的入住等关联操作
        customerRepository.deleteById(id);
    }

    @Override
    public boolean idCardExists(String idCardNumber) {
        return customerRepository.existsByIdCardNumber(idCardNumber);
    }

    @Override
    @Transactional
    public void deleteAllCustomers() {
        customerRepository.deleteAll();
    }
}